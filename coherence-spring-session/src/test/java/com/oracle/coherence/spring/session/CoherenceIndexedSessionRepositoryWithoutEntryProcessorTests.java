/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * Tests for {@link CoherenceIndexedSessionRepository} using embedded Coherence. For this test class
 * {@code useEntryProcessor} is set to @{code false}.
 *
 * @author Gunnar Hillert
 */
@DirtiesContext
@SpringJUnitWebConfig
class CoherenceIndexedSessionRepositoryWithoutEntryProcessorTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	@BeforeEach
	void setup() {
		super.expectedToUseEntryProcessor = false;
	}

	@EnableCoherenceHttpSession(useEntryProcessor = false)
	@EnableCoherence
	@Configuration
	static class CoherenceSessionConfig {
	}

}
