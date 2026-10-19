/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example;

import java.time.Duration;
import javax.management.AttributeNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.admin.SpringApplicationAdminMXBeanRegistrar;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.support.GenericApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the Boot application-admin readiness contract and diagnostic failures.
 */
class IsSpringUpTests {

	@Test
	void waitsForApplicationReadyEvent() throws Exception {
		try (GenericApplicationContext context = new GenericApplicationContext()) {
			context.refresh();
			SpringApplicationAdminMXBeanRegistrar registrar = new SpringApplicationAdminMXBeanRegistrar(IsSpringUp.OBJECT_NAME);
			registrar.setApplicationContext(context);
			registrar.afterPropertiesSet();
			try {
				assertThat(IsSpringUp.INSTANCE.call()).isFalse();
				registrar.onApplicationEvent(new ApplicationReadyEvent(new SpringApplication(), new String[0], context, Duration.ZERO));
				assertThat(IsSpringUp.INSTANCE.call()).isTrue();
			}
			finally {
				registrar.destroy();
			}
		}
	}

	@Test
	void waitsForAdminMBeanRegistration() throws Exception {
		assertThat(IsSpringUp.isReady(MBeanServerFactory.newMBeanServer())).isFalse();
	}

	@Test
	void preservesManagementFailure() throws Exception {
		MBeanServer server = mock(MBeanServer.class);
		AttributeNotFoundException failure = new AttributeNotFoundException("Ready attribute unavailable");
		when(server.getAttribute(new ObjectName(IsSpringUp.OBJECT_NAME), "Ready")).thenThrow(failure);
		assertThatThrownBy(() -> IsSpringUp.isReady(server)).isSameAs(failure);
	}

	@Test
	void preservesFatalFailure() throws Exception {
		MBeanServer server = mock(MBeanServer.class);
		LinkageError failure = new LinkageError("Application classes unavailable");
		when(server.getAttribute(new ObjectName(IsSpringUp.OBJECT_NAME), "Ready")).thenThrow(failure);
		assertThatThrownBy(() -> IsSpringUp.isReady(server)).isSameAs(failure);
	}
}
