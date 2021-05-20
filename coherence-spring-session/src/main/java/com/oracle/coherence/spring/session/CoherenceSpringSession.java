/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.SaveMode;
import org.springframework.session.Session;

/**
 * A custom implementation of Spring's {@link Session} interface that uses a {@link MapSession} as the
 * basis for its mapping and keeps track of changes that have been made since the last save.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
final class CoherenceSpringSession implements Session {

	private final CoherenceIndexedSessionRepository coherenceIndexedSessionRepository;
	private final MapSession delegate;

	private boolean isNew;

	private boolean sessionIdChanged;

	private boolean lastAccessedTimeChanged;

	private boolean maxInactiveIntervalChanged;

	private String originalId;

	private Map<String, Object> delta = new HashMap<>();

	CoherenceSpringSession(CoherenceIndexedSessionRepository coherenceIndexedSessionRepository, MapSession cached, boolean isNew) {
		this.coherenceIndexedSessionRepository = coherenceIndexedSessionRepository;
		this.delegate = cached;
		this.isNew = isNew;
		this.originalId = cached.getId();
		if (this.isNew || (coherenceIndexedSessionRepository.getSaveMode() == SaveMode.ALWAYS)) {
			getAttributeNames()
					.forEach((attributeName) -> this.delta.put(attributeName, cached.getAttribute(attributeName)));
		}
	}

	@Override
	public void setLastAccessedTime(Instant lastAccessedTime) {
		this.delegate.setLastAccessedTime(lastAccessedTime);
		this.lastAccessedTimeChanged = true;
		flushIfNeeded();
	}

	@Override
	public boolean isExpired() {
		return this.delegate.isExpired();
	}

	@Override
	public Instant getCreationTime() {
		return this.delegate.getCreationTime();
	}

	@Override
	public String getId() {
		return this.delegate.getId();
	}

	@Override
	public String changeSessionId() {
		final String newSessionId = this.delegate.changeSessionId();
		this.sessionIdChanged = true;
		return newSessionId;
	}

	@Override
	public Instant getLastAccessedTime() {
		return this.delegate.getLastAccessedTime();
	}

	@Override
	public void setMaxInactiveInterval(Duration interval) {
		this.delegate.setMaxInactiveInterval(interval);
		this.maxInactiveIntervalChanged = true;
		flushIfNeeded();
	}

	@Override
	public Duration getMaxInactiveInterval() {
		return this.delegate.getMaxInactiveInterval();
	}

	boolean isNew() {
		return this.isNew;
	}

	boolean isSessionIdChanged() {
		return this.sessionIdChanged;
	}

	boolean isLastAccessedTimeChanged() {
		return this.lastAccessedTimeChanged;
	}

	boolean isMaxInactiveIntervalChanged() {
		return this.maxInactiveIntervalChanged;
	}

	String getOriginalId() {
		return this.originalId;
	}

	Map<String, Object> getDelta() {
		return this.delta;
	}

	@Override
	public synchronized <T> T getAttribute(String attributeName) {
		final T attributeValue = this.delegate.getAttribute(attributeName);
		if (attributeValue != null
				&& this.coherenceIndexedSessionRepository.getSaveMode().equals(SaveMode.ON_GET_ATTRIBUTE)) {
			this.delta.put(attributeName, attributeValue);
		}
		return attributeValue;
	}

	@Override
	public Set<String> getAttributeNames() {
		return this.delegate.getAttributeNames();
	}

	@Override
	public void setAttribute(String attributeName, Object attributeValue) {
		this.delegate.setAttribute(attributeName, attributeValue);
		this.delta.put(attributeName, attributeValue);
		if (CoherenceIndexedSessionRepository.SPRING_SECURITY_CONTEXT.equals(attributeName)) {
			final Map<String, String> indexes = this.coherenceIndexedSessionRepository.getIndexResolver()
					.resolveIndexesFor(this);
			final String principal = (attributeValue != null) ? indexes.get(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME) : null;
			this.delegate.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, principal);
		}
		flushIfNeeded();
	}

	@Override
	public void removeAttribute(String attributeName) {
		setAttribute(attributeName, null);
	}

	MapSession getDelegate() {
		return this.delegate;
	}

	void setOriginalId(String originalId) {
		this.originalId = originalId;
	}

	boolean hasChanges() {
		return (this.lastAccessedTimeChanged || this.maxInactiveIntervalChanged || !this.delta.isEmpty());
	}

	void clearChangeFlags() {
		this.isNew = false;
		this.lastAccessedTimeChanged = false;
		this.sessionIdChanged = false;
		this.maxInactiveIntervalChanged = false;
		this.delta.clear();
	}

	void flushIfNeeded() {
		if (this.coherenceIndexedSessionRepository.getFlushMode() == FlushMode.IMMEDIATE) {
			this.coherenceIndexedSessionRepository.save(this);
		}
	}

}
