/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import com.oracle.coherence.spring.session.support.SessionEventApplicationListener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * Ensures that the expected SessionEvents are fired - using the "foobar" cache.
 *
 * @author Gunnar Hillert
 */
@DirtiesContext
@SpringJUnitWebConfig
@TestPropertySource(properties = {
		"coherence.tcmp.enabled = 'false'",
		"coherence.cluster.name = FoobarSessionEventTests"
})
public class FoobarSessionEventTests extends AbstractSessionEventTests {

	public FoobarSessionEventTests() {
		super.expectedCacheName = "foobar";
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceHttpSession(
			cache = "foobar",
			sessionTimeoutInSeconds = DEFAULT_SESSION_TIMEOUT_IN_SECONDS)
	static class CoherenceSessionConfig {
		@Bean
		SessionEventApplicationListener sessionEventRegistry() {
			return new SessionEventApplicationListener();
		}
	}
}
