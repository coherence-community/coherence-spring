/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.time.Duration;
import java.util.Map;

import com.oracle.coherence.common.base.Logger;
import com.oracle.coherence.spring.session.support.MyHttpSessionListener;
import com.oracle.coherence.spring.session.support.SessionEventApplicationListener;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.cache.CacheMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for {@link CoherenceIndexedSessionRepository} tests.
 * @author Gunnar Hillert
 *
 * @see CoherenceIndexedSessionRepositoryTests
 * @see ExtendSessionCoherenceIndexedSessionRepositoryTests
 * @see GrpcSessionCoherenceIndexedSessionRepositoryTests
 */
abstract class AbstractCoherenceIndexedSessionRepositoryTests {

	protected static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

	protected static final int DEFAULT_SESSION_TIMEOUT_IN_SECONDS = 1;

	protected String expectedCacheName;

	protected boolean expectedToUseEntryProcessor = true;

	@Autowired
	private Coherence coherenceInstance;

	@Autowired
	private CoherenceIndexedSessionRepository repository;

	@Autowired
	private SessionEventApplicationListener sessionEventRegistry;

	@Autowired
	private MyHttpSessionListener myHttpSessionListener;

	protected String sessionName;

	AbstractCoherenceIndexedSessionRepositoryTests() {
		String name = getLocalClusterName();
		if (name != null) {
			System.setProperty("coherence.cluster", getLocalClusterName());
		}
	}

	protected String getLocalClusterName() {
		return null;
	}

	@BeforeEach
	void setup(TestInfo testInfo) {
		this.myHttpSessionListener.reset();
		Logger.info("========== Starting test: " + testInfo.getDisplayName() + " ==========");
	}

	@AfterEach
	void tearDown(TestInfo testInfo) {
		this.myHttpSessionListener.reset();
		Logger.info("========== Finished test: " + testInfo.getDisplayName() + " ==========");
	}

	@Test
	void verifyCoherenceIndexedSessionRepositoryProperties() {
		assertThat(this.repository.isUseEntryProcessor()).isEqualTo(this.expectedToUseEntryProcessor);
	}

	@Test
	void createAndDestroyCoherenceSession() {
		final CoherenceSpringSession sessionToSave = this.repository.createSession();
		final String sessionId = sessionToSave.getId();
		this.repository.save(sessionToSave);

		final Session coherenceSession = StringUtils.hasText(this.sessionName)
				? this.coherenceInstance.getSession(this.sessionName) : this.coherenceInstance.getSession();
		final CacheMap<String, MapSession> cacheMap = coherenceSession
				.getCache(CoherenceIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);

		assertThat(cacheMap.get(sessionId)).isEqualTo(sessionToSave.getDelegate());

		assertThat(this.sessionEventRegistry.receivedEvent(sessionId)).isTrue();
		assertThat(this.sessionEventRegistry.<SessionCreatedEvent>getEvent(sessionId))
				.isInstanceOf(SessionCreatedEvent.class);
		this.sessionEventRegistry.clearSessionEvents();

		assertThat(this.myHttpSessionListener.getSessionsCreatedCount()).isEqualTo(1);
		assertThat(this.myHttpSessionListener.getSessionsDestroyedCount()).isEqualTo(0);

		this.repository.deleteById(sessionId);

		assertThat(cacheMap.get(sessionId)).isNull();

		this.sessionEventRegistry.getEvent(sessionId);

		assertThat(this.sessionEventRegistry.receivedEvent(sessionId)).isTrue();
		assertThat(this.sessionEventRegistry.<SessionDeletedEvent>getEvent(sessionId))
				.isInstanceOf(SessionDeletedEvent.class);

		assertThat(this.myHttpSessionListener.getSessionsCreatedCount()).isEqualTo(1);
		assertThat(this.myHttpSessionListener.getSessionsDestroyedCount()).isEqualTo(1);
	}

	@Test
	void addSessionAttributeToSession() {
		final CoherenceSpringSession toSave = this.repository.createSession();
		this.repository.save(toSave);

		final String attributeName = "foo-key";
		final String attributeValue = "foo-value";
		toSave.setAttribute(attributeName, attributeValue);
		this.repository.save(toSave);

		final CoherenceSpringSession sessionFromRepository = this.repository.findById(toSave.getId());
		assertThat(sessionFromRepository.<String>getAttribute(attributeName)).isEqualTo(attributeValue);

		this.repository.deleteById(toSave.getId());

		assertThat(this.repository.findById(toSave.getId())).isNull();
	}

	@Test
	void addSessionAttributeToSessionWithoutSaving() {
		final CoherenceSpringSession toSave = this.repository.createSession();
		this.repository.save(toSave);

		final String attributeName = "foo-key";
		final String attributeValue = "foo-value";
		toSave.setAttribute(attributeName, attributeValue);

		final CoherenceSpringSession sessionFromRepository = this.repository.findById(toSave.getId());
		assertThat(sessionFromRepository.<String>getAttribute(attributeName)).isNull();

		this.repository.deleteById(toSave.getId());

		assertThat(this.repository.findById(toSave.getId())).isNull();
	}

	@Test
	void addSessionAttributeToSessionWithImmediateFlushMode() {
		this.repository.setFlushMode(FlushMode.IMMEDIATE);
		final CoherenceSpringSession toSave = this.repository.createSession();
		this.repository.save(toSave);

		final String attributeName = "foo-key";
		final String attributeValue = "foo-value";
		toSave.setAttribute(attributeName, attributeValue);

		final CoherenceSpringSession sessionFromRepository = this.repository.findById(toSave.getId());
		assertThat(sessionFromRepository.<String>getAttribute(attributeName)).isEqualTo(attributeValue);

		this.repository.deleteById(toSave.getId());

		assertThat(this.repository.findById(toSave.getId())).isNull();
		this.repository.setFlushMode(FlushMode.ON_SAVE);
	}

	@Test
	void changeSessionIdOnce() {
		final String attributeName = "foo-key";
		final String attributeValue = "foo-value";
		final CoherenceSpringSession toSave = this.repository.createSession();
		toSave.setAttribute(attributeName, attributeValue);

		this.repository.save(toSave);

		final CoherenceSpringSession sessionFromRepository = this.repository.findById(toSave.getId());

		assertThat(sessionFromRepository.<String>getAttribute(attributeName)).isEqualTo(attributeValue);

		final String originalSessionId = sessionFromRepository.getId();
		final String newSessionId = sessionFromRepository.changeSessionId();

		this.repository.save(sessionFromRepository);

		assertThat(this.repository.findById(originalSessionId)).isNull();

		final CoherenceSpringSession sessionFromRepositoryWithNewSessionId = this.repository.findById(newSessionId);

		assertThat(sessionFromRepositoryWithNewSessionId.<String>getAttribute(attributeName)).isEqualTo(attributeValue);

		this.repository.deleteById(newSessionId);

		assertThat(this.repository.findById(newSessionId)).isNull();
	}

	@Test
	void changeSessionIdTwice() {
		final CoherenceSpringSession toSave = this.repository.createSession();

		this.repository.save(toSave);

		final String originalSessionId = toSave.getId();
		final String newSessionId1 = toSave.changeSessionId();
		final String newSessionId2 = toSave.changeSessionId();

		this.repository.save(toSave);

		assertThat(this.repository.findById(originalSessionId)).isNull();
		assertThat(this.repository.findById(newSessionId1)).isNull();
		assertThat(this.repository.findById(newSessionId2)).isNotNull();

		this.repository.deleteById(newSessionId2);
		assertThat(this.repository.findById(newSessionId2)).isNull();
	}

	@Test
	void changeSessionIdAndAddNewAttributeToChangedSession() {
		final CoherenceSpringSession sessionToSave = this.repository.createSession();
		this.repository.save(sessionToSave);

		final CoherenceSpringSession sessionFromRepository = this.repository.findById(sessionToSave.getId());

		final String attributeName = "foobar-key";
		final String attributeValue = "foobar-value";

		sessionFromRepository.setAttribute(attributeName, attributeValue);

		final String originalSessionId = sessionFromRepository.getId();
		final String newSessionId = sessionFromRepository.changeSessionId();

		this.repository.save(sessionFromRepository);

		assertThat(this.repository.findById(originalSessionId)).isNull();

		final CoherenceSpringSession findByChangeSessionId = this.repository.findById(newSessionId);

		assertThat(findByChangeSessionId.<String>getAttribute(attributeName)).isEqualTo(attributeValue);

		this.repository.deleteById(newSessionId);
		assertThat(this.repository.findById(newSessionId)).isNull();
	}

	@Test
	void changeSessionIdWhenSessionWasNotSavedYet() {
		final CoherenceSpringSession toSave = this.repository.createSession();
		final String originalSessionId = toSave.getId();
		toSave.changeSessionId();
		final String newSessionId = toSave.getId();
		this.repository.save(toSave);
		assertThat(this.repository.findById(newSessionId)).isNotNull();
		assertThat(this.repository.findById(originalSessionId)).isNull();

		this.repository.deleteById(toSave.getId());
	}

	@Test
	void changeSessionIdAfterSessionWasSavedWithImmediateFlushMode() {
		assertThat(this.repository.getFlushMode()).isEqualTo(FlushMode.ON_SAVE);
		this.repository.setFlushMode(FlushMode.IMMEDIATE);
		final CoherenceSpringSession toSave = this.repository.createSession();
		final String originalSessionId = toSave.getId();
		this.repository.save(toSave);

		toSave.changeSessionId();

		assertThat(this.repository.findById(toSave.getId())).isNull();
		assertThat(this.repository.findById(originalSessionId)).isNotNull();

		this.repository.deleteById(toSave.getId());
		this.repository.setFlushMode(FlushMode.ON_SAVE);
	}

	@Test
	void tryToUpdateSessionAfterDeletion() {
		final CoherenceSpringSession session = this.repository.createSession();
		final String sessionId = session.getId();
		this.repository.save(session);

		final CoherenceSpringSession sessionFromRepository = this.repository.findById(sessionId);
		sessionFromRepository.setAttribute("attributeName", "attributeValue");
		this.repository.deleteById(sessionId);
		this.repository.save(session);

		assertThat(this.repository.findById(sessionId)).isNull();
	}

	@Test
	void createAndUpdateSession() {
		final CoherenceSpringSession session = this.repository.createSession();
		final String sessionId = session.getId();

		this.repository.save(session);

		final CoherenceSpringSession sessionFromRepository = this.repository.findById(sessionId);
		sessionFromRepository.setAttribute("attributeName", "attributeValue");

		this.repository.save(sessionFromRepository);

		assertThat(this.repository.findById(sessionId)).isNotNull();

		this.repository.deleteById(sessionId);
		assertThat(this.repository.findById(sessionId)).isNull();
	}

	@Test
	void resetMaxInactiveIntervalForActiveSessions() {
		this.repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(100));

		final CoherenceSpringSession session1 = this.repository.createSession();
		final String sessionId1 = session1.getId();
		this.repository.save(session1);

		this.repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(200));

		final CoherenceSpringSession session2 = this.repository.createSession();
		final String sessionId2 = session2.getId();
		this.repository.save(session2);

		final CoherenceSpringSession sessionFromRepository1 = this.repository.findById(sessionId1);
		final CoherenceSpringSession sessionFromRepository2 = this.repository.findById(sessionId2);

		assertThat(sessionFromRepository1).isNotNull();
		assertThat(sessionFromRepository2).isNotNull();

		assertThat(sessionFromRepository1.getMaxInactiveInterval()).isEqualTo(Duration.ofSeconds(100));
		assertThat(sessionFromRepository2.getMaxInactiveInterval()).isEqualTo(Duration.ofSeconds(200));

		this.repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(50));
		this.repository.resetMaxInactiveIntervalForActiveSessions();

		assertThat(this.repository.findById(sessionId1).getMaxInactiveInterval()).isEqualTo(Duration.ofSeconds(50));
		assertThat(this.repository.findById(sessionId2).getMaxInactiveInterval()).isEqualTo(Duration.ofSeconds(50));
	}

	@Test
	void clearAllSessions() {
		this.repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(100));

		final CoherenceSpringSession session1 = this.repository.createSession();
		final String sessionId1 = session1.getId();
		this.repository.save(session1);

		this.repository.setDefaultMaxInactiveInterval(Duration.ofSeconds(200));

		final CoherenceSpringSession session2 = this.repository.createSession();
		final String sessionId2 = session2.getId();
		this.repository.save(session2);

		final CoherenceSpringSession sessionFromRepository1 = this.repository.findById(sessionId1);
		final CoherenceSpringSession sessionFromRepository2 = this.repository.findById(sessionId2);

		assertThat(sessionFromRepository1).isNotNull();
		assertThat(sessionFromRepository2).isNotNull();

		this.repository.clearAllSessions();

		assertThat(this.repository.findById(sessionId1)).isNull();
		assertThat(this.repository.findById(sessionId2)).isNull();
	}

	@Test
	void createSessionWithSecurityContext() {
		final CoherenceSpringSession session = this.repository.createSession();
		final String sessionId = session.getId();

		final Authentication authentication = new UsernamePasswordAuthenticationToken("eric.cartman",
				"password", AuthorityUtils.createAuthorityList("ROLE_USER"));
		final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		session.setAttribute(SPRING_SECURITY_CONTEXT, securityContext);

		this.repository.save(session);

		assertThat(this.repository.findById(sessionId)).isNotNull();

		this.repository.deleteById(sessionId);
		assertThat(this.repository.findById(sessionId)).isNull();
	}

	@Test
	void createSessionWithSecurityContextAndFindByPrincipal() {
		final CoherenceSpringSession session = this.repository.createSession();

		final String username = "coherence_rocks";
		final Authentication authentication = new UsernamePasswordAuthenticationToken(username, "password",
				AuthorityUtils.createAuthorityList("ROLE_USER"));
		final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
		securityContext.setAuthentication(authentication);
		session.setAttribute(SPRING_SECURITY_CONTEXT, securityContext);

		this.repository.save(session);

		final CoherenceSpringSession sessionWithoutSecurityContext = this.repository.createSession();
		sessionWithoutSecurityContext.setAttribute("foo", "bar");
		this.repository.save(sessionWithoutSecurityContext);

		final Map<String, CoherenceSpringSession> sessions = this.repository
				.findByIndexNameAndIndexValue(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, username);
		assertThat(sessions).hasSize(1);
		final CoherenceSpringSession coherenceSpringSession = sessions.values().iterator().next();
		final SecurityContext retrievedSecurityContext = coherenceSpringSession.getAttribute(SPRING_SECURITY_CONTEXT);
		assertThat(retrievedSecurityContext.getAuthentication().getName()).isEqualTo("coherence_rocks");
		this.repository.deleteById(session.getId());
		assertThat(this.repository.findById(session.getId())).isNull();
	}

	@Configuration
	static class CommonConfig {
		@Bean
		SessionEventApplicationListener sessionEventRegistry() {
			return new SessionEventApplicationListener();
		}

		@Bean
		MyHttpSessionListener myHttpSessionListener() {
			return new MyHttpSessionListener();
		}
	}
}
