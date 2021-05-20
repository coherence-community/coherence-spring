/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.events;

import com.oracle.coherence.spring.annotation.event.Deleted;
import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;
import com.tangosol.net.cache.CacheEvent;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.util.MapEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.session.MapSession;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;

/**
 * Registers Coherence eventhandlers to handle the following session events:
 *
 * <ul>
 *     <li>Session Creation
 *     <li>Session Expiration
 *     <li>Session Deletion
 * </ul>
 *
 * The handled events are then propagated as Spring application events via Spring's {@link ApplicationEventPublisher}.
 * Specifically:
 *
 * <ul>
 *     <li>{@link SessionCreatedEvent}
 *     <li>{@link SessionExpiredEvent}
 *     <li>{@link SessionDeletedEvent}
 * </ul>
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceSessionEventHandler implements ApplicationEventPublisherAware {

	private static final Log logger = LogFactory.getLog(CoherenceSessionEventHandler.class);

	private ApplicationEventPublisher eventPublisher;

	@CoherenceEventListener
	public void entryAdded(@Inserted @MapName(CoherenceIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME) EntryEvent<String, MapSession> event) {
		MapSession session = event.getValue();
		if (session.getId().equals(session.getOriginalId())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Session created with id: " + session.getId());
			}
			this.eventPublisher.publishEvent(new SessionCreatedEvent(this, session));
		}
	}

	@CoherenceEventListener
	public void entryRemoved(@Deleted @MapName(CoherenceIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME) MapEvent<String, MapSession> event) {
		MapSession session = event.getOldValue();
		if (session != null) {
			if (event instanceof CacheEvent && ((CacheEvent) event).isSynthetic()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Session expired with id: " + session.getId());
				}
				this.eventPublisher.publishEvent(new SessionExpiredEvent(this, event.getOldValue()));
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Session deleted with id: " + session.getId());
				}
				this.eventPublisher.publishEvent(new SessionDeletedEvent(this, session));
			}
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}
}
