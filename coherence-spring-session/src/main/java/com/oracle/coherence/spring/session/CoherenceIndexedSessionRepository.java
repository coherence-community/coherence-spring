/*
 * Copyright (c) 2021, 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.oracle.coherence.spring.session.events.CoherenceSessionEventMapListener;
import com.oracle.coherence.spring.session.support.PrincipalNameExtractor;
import com.tangosol.coherence.memcached.server.MemcachedHelper;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.Filter;
import com.tangosol.util.filter.EqualsFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.DelegatingIndexResolver;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.FlushMode;
import org.springframework.session.IndexResolver;
import org.springframework.session.MapSession;
import org.springframework.session.PrincipalNameIndexResolver;
import org.springframework.session.SaveMode;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.events.AbstractSessionEvent;
import org.springframework.util.Assert;

/**
 * The {@link CoherenceIndexedSessionRepository} is a {@link SessionRepository}
 * implementation that stores sessions in Coherence's distributed {@link CacheMap}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceIndexedSessionRepository implements FindByIndexNameSessionRepository<CoherenceSpringSession>,
		ApplicationEventPublisherAware {

	/**
	 * The default name of map used by Spring Session to store sessions.
	 */
	public static final String DEFAULT_SESSION_MAP_NAME = "spring:session:sessions";

	/**
	 * The principal name custom attribute name.
	 */
	public static final String PRINCIPAL_NAME_ATTRIBUTE = "principalName";

	static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

	private static final Log logger = LogFactory.getLog(CoherenceIndexedSessionRepository.class);

	private final com.tangosol.net.Session coherenceSession;

	private ApplicationEventPublisher eventPublisher;

	/**
	 * If non-null, this value is used to override
	 * {@link MapSession#setMaxInactiveInterval(Duration)}.
	 */
	private Duration defaultMaxInactiveInterval;

	private IndexResolver<Session> indexResolver = new DelegatingIndexResolver<>(new PrincipalNameIndexResolver<>());

	private String sessionMapName = DEFAULT_SESSION_MAP_NAME;

	private FlushMode flushMode = FlushMode.ON_SAVE;

	private SaveMode saveMode = SaveMode.ON_SET_ATTRIBUTE;

	private NamedCache<String, MapSession> sessionCache;

	/**
	 * Shall a Coherence Entry Processor be used for handling updates to session? Defaults to true.
	 */
	private boolean useEntryProcessor = true;

	/**
	 * Create a new {@link CoherenceIndexedSessionRepository} instance.
	 * @param coherenceSession the Coherence {@link com.tangosol.net.Session} instance to use for managing sessions
	 */
	public CoherenceIndexedSessionRepository(com.tangosol.net.Session coherenceSession) {
		Assert.notNull(coherenceSession, "CoherenceSession must not be null");
		this.coherenceSession = coherenceSession;
	}

	@PostConstruct
	public void init() {
		this.sessionCache = this.coherenceSession.getCache(this.sessionMapName);

		final CoherenceSessionEventMapListener sessionRemovedMapListener = new CoherenceSessionEventMapListener(this.eventPublisher);
		this.sessionCache.addMapListener(sessionRemovedMapListener);

		if (logger.isDebugEnabled()) {
			final String maxInactiveInterval =
					(this.defaultMaxInactiveInterval != null) ? String.valueOf(this.defaultMaxInactiveInterval.getSeconds()) : "null";
			logger.debug(String.format("CoherenceIndexedSessionRepository initialized with "
							+ "[Scope: '%s'; cache: '%s'; defaultMaxInactiveInterval: %ssec; useEntryProcessor: %s]",
					this.coherenceSession.getScopeName(), this.sessionCache.getCacheName(),
					maxInactiveInterval, this.useEntryProcessor));
		}
	}

	@PreDestroy
	public void close() {
		this.sessionCache.close();
	}

	/**
	 * Set the maximum inactive interval between requests before newly created sessions will be invalidated.
	 * A value of {@code 0} means that the session will never time out unless the cache is configured otherwise in the
	 * {@code coherence-cache-config.xml}. The default is 1800s (30 minutes).
	 * @param defaultMaxInactiveInterval the maximum inactive interval in seconds must not be negative or null
	 */
	public void setDefaultMaxInactiveInterval(Duration defaultMaxInactiveInterval) {
		Assert.notNull(defaultMaxInactiveInterval, "defaultMaxInactiveInterval must not be null");
		Assert.isTrue(defaultMaxInactiveInterval.toMillis() >= 0, "defaultMaxInactiveInterval must not be negative");
		this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
	}

	/**
	 * Set the {@link IndexResolver} to use.
	 * @param indexResolver the index resolver
	 */
	public void setIndexResolver(IndexResolver<Session> indexResolver) {
		Assert.notNull(indexResolver, "indexResolver cannot be null");
		this.indexResolver = indexResolver;
	}

	/**
	 * Set the name of map used to store sessions.
	 * @param sessionMapName the session map name
	 */
	public void setSessionMapName(String sessionMapName) {
		Assert.hasText(sessionMapName, "Map name must not be empty");
		this.sessionMapName = sessionMapName;
	}

	/**
	 * Sets the {@link FlushMode}. Defaults to {@link FlushMode#ON_SAVE}.
	 * @param flushMode must not be null
	 */
	public void setFlushMode(FlushMode flushMode) {
		Assert.notNull(flushMode, "flushMode must not be null");
		this.flushMode = flushMode;
	}

	/**
	 * Set the save mode.
	 * @param saveMode must not be null
	 */
	public void setSaveMode(SaveMode saveMode) {
		Assert.notNull(saveMode, "saveMode must not be null");
		this.saveMode = saveMode;
	}

	public void setUseEntryProcessor(boolean useEntryProcessor) {
		this.useEntryProcessor = useEntryProcessor;
	}

	public boolean isUseEntryProcessor() {
		return this.useEntryProcessor;
	}

	@Override
	public CoherenceSpringSession createSession() {
		MapSession cached = new MapSession();
		if (this.defaultMaxInactiveInterval != null) {
			cached.setMaxInactiveInterval(this.defaultMaxInactiveInterval);
		}
		final CoherenceSpringSession session = new CoherenceSpringSession(this, cached, true);
		session.flushIfNeeded();
		return session;
	}

	@Override
	public void save(CoherenceSpringSession session) {
		final long maxInactiveIntervalMillis = session.getMaxInactiveInterval().toMillis();
		final boolean expireCacheEntry = maxInactiveIntervalMillis > 0;

		if (session.isNew()) {
			if (expireCacheEntry) {
				this.sessionCache.put(session.getId(), session.getDelegate(), maxInactiveIntervalMillis);
			}
			else {
				this.sessionCache.put(session.getId(), session.getDelegate());
			}
		}
		else if (session.isSessionIdChanged()) {
			this.sessionCache.remove(session.getOriginalId());
			session.setOriginalId(session.getId());

			if (expireCacheEntry) {
				this.sessionCache.put(session.getId(), session.getDelegate(), maxInactiveIntervalMillis);
			}
			else {
				this.sessionCache.put(session.getId(), session.getDelegate());
			}
		}
		else if (session.hasChanges()) {
			if (this.isUseEntryProcessor()) {
				final SessionUpdateEntryProcessor entryProcessor = new SessionUpdateEntryProcessor();
				entryProcessor.setDefaultMaxInactiveInterval(this.defaultMaxInactiveInterval);
				if (session.isLastAccessedTimeChanged()) {
					entryProcessor.setLastAccessedTime(session.getLastAccessedTime());
				}
				if (session.isMaxInactiveIntervalChanged()) {
					entryProcessor.setMaxInactiveInterval(session.getMaxInactiveInterval());
				}
				if (!session.getDelta().isEmpty()) {
					entryProcessor.setDelta(new HashMap<>(session.getDelta()));
				}
				this.sessionCache.invoke(session.getId(), entryProcessor);
			}
			else {
				if (expireCacheEntry) {
					this.sessionCache.put(session.getId(), session.getDelegate(), maxInactiveIntervalMillis);
				}
				else {
					this.sessionCache.put(session.getId(), session.getDelegate());
				}
			}
		}
		session.clearChangeFlags();

	}

	@Override
	public CoherenceSpringSession findById(String id) {
		MapSession saved = this.sessionCache.get(id);
		if (saved == null) {
			return null;
		}
		if (saved.isExpired()) {
			deleteById(saved.getId());
			return null;
		}
		return new CoherenceSpringSession(this, saved, false);
	}

	@Override
	public void deleteById(String id) {
		this.sessionCache.remove(id);
	}

	@Override
	public Map<String, CoherenceSpringSession> findByIndexNameAndIndexValue(String indexName, String indexValue) {
		if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
			return Collections.emptyMap();
		}
		final Filter<?> filter = new EqualsFilter<>(new PrincipalNameExtractor(), indexValue);
		final Set<Map.Entry<String, MapSession>> sessions = this.sessionCache.entrySet(filter);

		final Map<String, CoherenceSpringSession> sessionMap = new HashMap<>(sessions.size());
		for (Map.Entry<String, MapSession> session : sessions) {
			sessionMap.put(session.getValue().getId(), new CoherenceSpringSession(this, session.getValue(), false));
		}
		return sessionMap;
	}

	public FlushMode getFlushMode() {
		return this.flushMode;
	}

	public SaveMode getSaveMode() {
		return this.saveMode;
	}

	public IndexResolver<Session> getIndexResolver() {
		return this.indexResolver;
	}

	/**
	 * Sets the {@link ApplicationEventPublisher} that is used to publish
	 * {@link AbstractSessionEvent session events}. The default is to not publish session
	 * events.
	 * @param applicationEventPublisher the {@link ApplicationEventPublisher} that is used
	 *                                  to publish session events. Cannot be null.
	 */
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		Assert.notNull(applicationEventPublisher, "ApplicationEventPublisher cannot be null");
		this.eventPublisher = applicationEventPublisher;
	}

	/**
	 * Clear all sessions.
	 */
	public void clearAllSessions() {
		this.sessionCache.truncate();
	}

	/**
	 * Reset the max inactive interval for all active sessions.
	 */
	public void resetMaxInactiveIntervalForActiveSessions() {
		final long expirationInMillis = this.defaultMaxInactiveInterval.toMillis();
		this.sessionCache.invokeAll((entry) -> {

			final MapSession mapSession = entry.getValue();

			if (entry.getValue().isExpired()) {
				return null;
			}

			mapSession.setMaxInactiveInterval(Duration.ofMillis(expirationInMillis));
			final BinaryEntry binaryEntry = MemcachedHelper.getBinaryEntry(entry);
			binaryEntry.expire(expirationInMillis);

			entry.setValue(mapSession);
			return null;
		});
	}
}
