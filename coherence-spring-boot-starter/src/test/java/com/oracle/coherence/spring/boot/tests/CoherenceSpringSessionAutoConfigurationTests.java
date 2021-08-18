/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceProperties;
import com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.session.SessionRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceSpringSessionAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withConfiguration(AutoConfigurations.of(CoherenceProperties.class))
			.withConfiguration(AutoConfigurations.of(SessionProperties.class))
			.withConfiguration(AutoConfigurations.of(ServerProperties.class))
			.withConfiguration(AutoConfigurations.of(CoherenceSpringSessionAutoConfiguration.class))
			.withInitializer(new ConfigDataApplicationContextInitializer());

	@Test
	void testAutoConfigurationDisabledWithStoreTypeSetToNone() {
		this.contextRunner.withPropertyValues("spring.session.store-type=none")
				.run((context) -> assertThat(context).doesNotHaveBean(SessionRepository.class));
	}

	@Test
	void testAutoConfigurationDisabledWithCoherenceSpringSessionDisabled() {
		this.contextRunner.withPropertyValues("coherence.spring.session.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean(SessionRepository.class));
	}

	@Test
	void testAutoConfigurationDisabledWithCoherenceSpringSessionEnabled() {
		this.contextRunner.withPropertyValues("coherence.spring.session.enabled=true")
				.run((context) -> assertThat(context).hasSingleBean(SessionRepository.class));
	}

}
