/*
 * Copyright (c) 2021, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure.session;

import java.time.Duration;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;
import com.oracle.coherence.spring.session.config.annotation.web.http.CoherenceHttpSessionConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.session.autoconfigure.SessionProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.session.SessionRepository;

/**
 * Coherence auto-configuration for Spring Session.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@AutoConfigureAfter(CoherenceAutoConfiguration.class)
@AutoConfiguration
@EnableConfigurationProperties(CoherenceSpringSessionProperties.class)
@Conditional(CoherenceSpringSessionCondition.class)
@ConditionalOnClass(CoherenceIndexedSessionRepository.class)
@ConditionalOnMissingBean(SessionRepository.class)
@ConditionalOnProperty(name = "coherence.spring.session.enabled", havingValue = "true", matchIfMissing = true)
public class CoherenceSpringSessionAutoConfiguration {

	@AutoConfiguration
	@AutoConfigureBefore(name = "org.springframework.boot.session.autoconfigure.SessionsEndpointAutoConfiguration")
	@ConditionalOnClass(name = "org.springframework.boot.session.autoconfigure.SessionProperties")
	public static class SpringBootCoherenceHttpSessionConfiguration extends CoherenceHttpSessionConfiguration {

		@Autowired
		public void customize(SessionProperties sessionProperties,
				CoherenceSpringSessionProperties coherenceSpringSessionProperties, ServerProperties serverProperties) {
			Duration timeout = sessionProperties.getTimeout();
			if (timeout == null) {
				timeout = serverProperties.getServlet().getSession().getTimeout();
			}
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
