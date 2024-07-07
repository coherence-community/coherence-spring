/*
 * Copyright (c) 2021, 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.events;

import com.oracle.coherence.spring.session.support.SessionDebugMessageUtils;
import com.oracle.coherence.spring.session.support.SessionEvent;
import com.tangosol.net.cache.CacheEvent;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.session.MapSession;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.util.Assert;

/**
 * Custom Map Event that subscribes to Coherence MapEvents. The handled event is then propagated as a
 * Spring application event via Spring's {@link ApplicationEventPublisher}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceSessionEventMapListener implements MapListener<String, MapSession> {

	private static final Log logger = LogFactory.getLog(CoherenceSessionEventMapListener.class);

	private final ApplicationEventPublisher eventPublisher;

	public CoherenceSessionEventMapListener(ApplicationEventPublisher eventPublisher) {
		Assert.notNull(eventPublisher, "eventPublisher must not be null");
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void entryInserted(MapEvent<String, MapSession> event) {
		MapSession session = event.getNewValue();
		if (event instanceof CacheEvent && session != null) {
			if (logger.isDebugEnabled()) {
				logger.debug(SessionDebugMessageUtils.createSessionEventMessage(SessionEvent.CREATED, session));
			}
			this.eventPublisher.publishEvent(new SessionCreatedEvent(this, session));
		}
	}

	@Override
	public void entryUpdated(MapEvent<String, MapSession> event) {
	}

	@Override
	public void entryDeleted(MapEvent<String, MapSession> event) {
		MapSession session = event.getOldValue();
		if (session != null) {
			if (event instanceof CacheEvent && ((CacheEvent) event).isSynthetic()) {
				if (logger.isDebugEnabled()) {
					logger.debug(SessionDebugMessageUtils.createSessionEventMessage(SessionEvent.EXPIRED, session));
				}
				this.eventPublisher.publishEvent(new SessionExpiredEvent(this, event.getOldValue()));
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug(SessionDebugMessageUtils.createSessionEventMessage(SessionEvent.DELETED, session));
				}
				this.eventPublisher.publishEvent(new SessionDeletedEvent(this, session));
			}
		}
	}

}
