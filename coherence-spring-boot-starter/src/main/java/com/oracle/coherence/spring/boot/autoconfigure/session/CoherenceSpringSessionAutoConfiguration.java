/*
 * Copyright (c) 2021, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure.session;

import java.time.Duration;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.session.config.annotation.web.http.CoherenceHttpSessionConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.session.autoconfigure.SessionTimeout;
import org.springframework.context.annotation.Conditional;

/**
 * Coherence auto-configuration for Spring Session.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@AutoConfiguration(after = CoherenceAutoConfiguration.class,
		beforeName = {"org.springframework.boot.session.autoconfigure.SessionAutoConfiguration",
				"org.springframework.boot.session.autoconfigure.SessionsEndpointAutoConfiguration"})
@Conditional(CoherenceSpringSessionCondition.class)
@ConditionalOnClass(name = {"com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository",
		"com.oracle.coherence.spring.session.config.annotation.web.http.CoherenceHttpSessionConfiguration",
		"org.springframework.session.SessionRepository", "org.springframework.session.web.http.SessionRepositoryFilter",
		"org.springframework.session.FlushMode", "org.springframework.session.SaveMode",
		"org.springframework.boot.session.autoconfigure.SessionProperties",
		"org.springframework.boot.session.autoconfigure.SessionTimeout",
		"jakarta.servlet.Servlet", "jakarta.servlet.Filter", "jakarta.servlet.http.HttpSession"})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnMissingBean(type = "org.springframework.session.SessionRepository")
@ConditionalOnProperty(name = "coherence.spring.session.enabled", havingValue = "true", matchIfMissing = true)
public class CoherenceSpringSessionAutoConfiguration {

	@AutoConfiguration
	@EnableConfigurationProperties(CoherenceSpringSessionProperties.class)
	public static class SpringBootCoherenceHttpSessionConfiguration extends CoherenceHttpSessionConfiguration {

		@Autowired
		public void customize(SessionTimeout sessionTimeout,
				CoherenceSpringSessionProperties coherenceSpringSessionProperties) {
			Duration timeout = sessionTimeout.getTimeout();
			if (timeout != null) {
				setMaxInactiveIntervalInSeconds((int) timeout.getSeconds());
			}
			setSessionMapName(coherenceSpringSessionProperties.getMapName());
			setFlushMode(coherenceSpringSessionProperties.getFlushMode());
			setSaveMode(coherenceSpringSessionProperties.getSaveMode());
			setUseEntryProcessor(coherenceSpringSessionProperties.getUseEntryProcessor());
		}
	}
}
