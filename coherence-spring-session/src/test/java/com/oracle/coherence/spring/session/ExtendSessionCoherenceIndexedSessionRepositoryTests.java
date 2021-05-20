/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import com.oracle.coherence.spring.test.junit.CoherenceServerJunitExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * Tests for {@link CoherenceIndexedSessionRepository} using Coherence*Extend.
 *
 * @author Gunnar Hillert
 */
@ExtendWith(CoherenceServerJunitExtension.class)
@DirtiesContext
@SpringJUnitWebConfig
public class ExtendSessionCoherenceIndexedSessionRepositoryTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	public ExtendSessionCoherenceIndexedSessionRepositoryTests() {
		super.sessionName = "coherence_extend";
	}

	@Configuration
	@EnableCoherenceHttpSession(session = "coherence_extend") //TODO Derive from single session
	@EnableCoherence
	static class CoherenceConfig {
		@Bean
		SessionConfigurationBean sessionConfigurationBean() {
			SessionConfigurationBean sb = new SessionConfigurationBean();
			sb.setType(SessionType.CLIENT);
			sb.setName("coherence_extend");
			return sb;
		}
	}
}
