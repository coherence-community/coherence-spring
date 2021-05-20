/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.awaitility.core.ConditionTimeoutException;

import org.springframework.context.ApplicationListener;
import org.springframework.session.events.AbstractSessionEvent;
import org.springframework.util.Assert;

import static org.awaitility.Awaitility.await;

/**
 * Implementation of an {@link ApplicationListener} that handles {@link AbstractSessionEvent}s in support for
 * the {@link com.oracle.coherence.spring.session.SessionEventCoherenceIndexedSessionRepositoryTests}.
 *
 * @author Gunnar Hillert
 */
public class SessionEventApplicationListener implements ApplicationListener<AbstractSessionEvent> {

	public static final int DEFAULT_TIMEOUT = 5000;

	private final Map<String, AbstractSessionEvent> sessionEvents = new ConcurrentHashMap<>();

	/**
	 * Handles the {@link AbstractSessionEvent} and adds the event to a {@link #sessionEvents} for further retrieval.
	 *
	 * @param event the event to handle
	 * @see #getEvent(String)
	 *
	 */
	@Override
	public void onApplicationEvent(AbstractSessionEvent event) {
		final String sessionId = event.getSessionId();
		this.sessionEvents.put(sessionId, event);
	}

	/**
	 * Clear the underlying {@link Map} that contains the events.
	 */
	public void clearSessionEvents() {
		this.sessionEvents.clear();
	}

	/**
	 * Helper method that waits for {@link #DEFAULT_TIMEOUT} milliseconds for the presence of an
	 * {@link AbstractSessionEvent} in the underlying {@link Map}.
	 * @param sessionId must not be null or empty
	 */
	private void waitForEvent(String sessionId) {
		Assert.hasText(sessionId, "The provided sessionId must not be null or empty.");
		await().atMost(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS).until(() -> this.sessionEvents.containsKey(sessionId));
	}

	/**
	 * Helper method that waits for {@link #DEFAULT_TIMEOUT} milliseconds for the presence of an
	 * {@link AbstractSessionEvent} in the underlying {@link Map}.
	 * @param sessionId must not be null or empty
	 * @return false if the event was not present within {@link #DEFAULT_TIMEOUT} milliseconds
	 */
	public boolean receivedEvent(String sessionId) {
		try {
			waitForEvent(sessionId);
		}
		catch (ConditionTimeoutException ex) {
			return false;
		}
		return true;
	}

	/**
	 * Helper method that waits for {@link #DEFAULT_TIMEOUT} milliseconds for the presence of an
	 * {@link AbstractSessionEvent} in the underlying {@link Map}.
	 * @param sessionId must not be null or empty
	 * @return the {@link AbstractSessionEvent}
	 * @throws ConditionTimeoutException if the timeout is exceeded.
	 */
	public <E extends AbstractSessionEvent> E getEvent(String sessionId) {
		waitForEvent(sessionId);
		return (E) this.sessionEvents.get(sessionId);
	}

}
