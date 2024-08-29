/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import com.oracle.coherence.spring.session.support.FixedSessionIdGenerator;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.cache.CacheMap;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSession;
import org.springframework.session.SessionIdGenerator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for providing a custom {@link SessionIdGenerator} using embedded Coherence.
 *
 * @author Gunnar Hillert
 */
@DirtiesContext
@SpringJUnitWebConfig
class CustomSessionIdGeneratorTests {

	private static final String SESSION_ID = "test-session-id";

	@Autowired
	private Coherence coherenceInstance;

	@Autowired
	protected CoherenceIndexedSessionRepository repository;

	CustomSessionIdGeneratorTests() {
		System.setProperty("coherence.cluster", "CustomSessionIdGeneratorTests");
		System.setProperty("coherence.ttl", "0");
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("coherence.wka", "127.0.0.1");
	}

	@Test
	void createAndDestroyCoherenceSessionAndValidatedSessionId() {

		final CoherenceSpringSession sessionToSave = this.repository.createSession();
		final String sessionId = sessionToSave.getId();

		assertThat(sessionId).isEqualTo(SESSION_ID);

		this.repository.save(sessionToSave);

		final Session coherenceSession = this.coherenceInstance.getSession();
		final CacheMap<String, MapSession> cacheMap = coherenceSession
				.getCache(CoherenceIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);

		assertThat(cacheMap.get(SESSION_ID)).isEqualTo(sessionToSave.getDelegate());

		this.repository.deleteById(SESSION_ID);

		assertThat(cacheMap.get(SESSION_ID)).isNull();

	}

	@EnableCoherenceHttpSession
	@EnableCoherence
	@Configuration
	static class CoherenceSessionConfig {
		@Bean
		SessionIdGenerator sessionIdGenerator() {
			return new FixedSessionIdGenerator(SESSION_ID);
		}
	}

}
