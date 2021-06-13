/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.time.Duration;
import java.time.Instant;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import com.oracle.coherence.spring.session.support.SessionEventApplicationListener;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ensures that the expected SessionEvents are fired.
 *
 * @author Gunnar Hillert
 */
public abstract class AbstractSessionEventTests {

	private static final Log logger = LogFactory.getLog(AbstractSessionEventTests.class);

	protected static final int DEFAULT_SESSION_TIMEOUT_IN_SECONDS = 1;

	protected String expectedCacheName;

	@Autowired
	private Coherence coherence;

	@Autowired
	private FindByIndexNameSessionRepository<Session> repository;

	@Autowired
	private SessionEventApplicationListener sessionEventApplicationListener;

	@BeforeEach
	void setup() {
		this.sessionEventApplicationListener.clearSessionEvents();
	}

	@Test
	void saveSessionTest() throws InterruptedException {
		final NamedCache sessionCache = this.coherence.getSession().getCache(this.expectedCacheName);
		assertThat(sessionCache.isActive());
		assertThat(sessionCache.size()).isEqualTo(0);

		final String username = "coherence_rocks";

		final Session sessionToSave = this.repository.createSession();

		final String expectedAttributeName = "foo-key";
		final String expectedAttributeValue = "foo-value";
		sessionToSave.setAttribute(expectedAttributeName, expectedAttributeValue);
		final Authentication toSaveToken = new UsernamePasswordAuthenticationToken(username, "password",
				AuthorityUtils.createAuthorityList("ROLE_USER"));
		final SecurityContext toSaveContext = SecurityContextHolder.createEmptyContext();
		toSaveContext.setAuthentication(toSaveToken);
		sessionToSave.setAttribute("SPRING_SECURITY_CONTEXT", toSaveContext);
		sessionToSave.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, username);

		this.repository.save(sessionToSave);

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionCreatedEvent>getEvent(sessionToSave.getId()))
				.isInstanceOf(SessionCreatedEvent.class);

		final Session sessionFromRepository = this.repository.findById(sessionToSave.getId());

		assertThat(sessionFromRepository.getId()).isEqualTo(sessionToSave.getId());
		assertThat(sessionFromRepository.getAttributeNames()).isEqualTo(sessionToSave.getAttributeNames());
		assertThat(sessionFromRepository.<String>getAttribute(expectedAttributeName))
				.isEqualTo(sessionToSave.getAttribute(expectedAttributeName));

		assertThat(sessionCache.size()).isEqualTo(1);

		this.repository.deleteById(sessionToSave.getId());
	}

	@Test
	void expiredSessionTest() throws InterruptedException {
		final Session sessionToSave = this.repository.createSession();
		this.repository.save(sessionToSave);

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionCreatedEvent>getEvent(sessionToSave.getId()))
				.isInstanceOf(SessionCreatedEvent.class);
		this.sessionEventApplicationListener.clearSessionEvents();

		assertThat(sessionToSave.getMaxInactiveInterval())
				.isEqualTo(Duration.ofSeconds(DEFAULT_SESSION_TIMEOUT_IN_SECONDS));

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionExpiredEvent>getEvent(sessionToSave.getId()))
				.isInstanceOf(SessionExpiredEvent.class);
		assertThat(this.repository.findById(sessionToSave.getId())).isNull();

		this.repository.deleteById(sessionToSave.getId());

	}

	@Test
	void deletedSessionTest() throws InterruptedException {
		final Session sessionToSave = this.repository.createSession();
		this.repository.save(sessionToSave);

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionCreatedEvent>getEvent(sessionToSave.getId()))
				.isInstanceOf(SessionCreatedEvent.class);
		this.sessionEventApplicationListener.clearSessionEvents();

		this.repository.deleteById(sessionToSave.getId());

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionDeletedEvent>getEvent(sessionToSave.getId()))
				.isInstanceOf(SessionDeletedEvent.class);

		assertThat(this.repository.findById(sessionToSave.getId())).isNull();
	}

	@Test
	void saveUpdatesTimeToLiveTest() {
		final Session sessionToSave = this.repository.createSession();
		sessionToSave.setMaxInactiveInterval(Duration.ofSeconds(3));
		this.repository.save(sessionToSave);

		// Get and save the session like SessionRepositoryFilter would.
		final Session sessionToUpdate = this.repository.findById(sessionToSave.getId());
		sessionToUpdate.setLastAccessedTime(Instant.now());
		this.repository.save(sessionToUpdate);
		assertThat(this.repository.findById(sessionToUpdate.getId())).isNotNull();

		this.repository.deleteById(sessionToUpdate.getId());

	}

	@Test
	void changeSessionIdNoEventTest() {
		final Session sessionToSave = this.repository.createSession();
		sessionToSave.setMaxInactiveInterval(Duration.ofMinutes(30));
		this.repository.save(sessionToSave);

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionCreatedEvent>getEvent(sessionToSave.getId()))
				.isInstanceOf(SessionCreatedEvent.class);
		this.sessionEventApplicationListener.clearSessionEvents();

		sessionToSave.changeSessionId();
		this.repository.save(sessionToSave);

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isFalse();

		this.repository.deleteById(sessionToSave.getId());
	}

	@Test
	void updateMaxInactiveIntervalTest() throws InterruptedException {
		final Session sessionToSave = this.repository.createSession();
		sessionToSave.setMaxInactiveInterval(Duration.ofMinutes(30));
		this.repository.save(sessionToSave);

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToSave.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionCreatedEvent>getEvent(sessionToSave.getId()))
				.isInstanceOf(SessionCreatedEvent.class);
		this.sessionEventApplicationListener.clearSessionEvents();

		final Session sessionToUpdate = this.repository.findById(sessionToSave.getId());
		sessionToUpdate.setLastAccessedTime(Instant.now());
		sessionToUpdate.setMaxInactiveInterval(Duration.ofSeconds(1));
		this.repository.save(sessionToUpdate);

		assertThat(this.sessionEventApplicationListener.receivedEvent(sessionToUpdate.getId())).isTrue();
		assertThat(this.sessionEventApplicationListener.<SessionExpiredEvent>getEvent(sessionToUpdate.getId()))
				.isInstanceOf(SessionExpiredEvent.class);
		assertThat(this.repository.findById(sessionToUpdate.getId())).isNull();

		this.repository.deleteById(sessionToSave.getId());
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceHttpSession(sessionTimeoutInSeconds = DEFAULT_SESSION_TIMEOUT_IN_SECONDS)
	static class CoherenceSessionConfig {
		@Bean
		SessionEventApplicationListener sessionEventRegistry() {
			return new SessionEventApplicationListener();
		}
	}
}
