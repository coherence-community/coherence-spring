/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.events;

import com.tangosol.net.cache.CacheEvent;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.session.MapSession;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.util.Assert;

/**
 * Custom Map Event that handles the deletion and expiration of a session. The handled event is then propagated as a
 * Spring application event via Spring's {@link ApplicationEventPublisher}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class SessionRemovedMapListener implements MapListener<String, MapSession> {

	private static final Log logger = LogFactory.getLog(SessionRemovedMapListener.class);

	private final ApplicationEventPublisher eventPublisher;

	public SessionRemovedMapListener(ApplicationEventPublisher eventPublisher) {
		Assert.notNull(eventPublisher, "eventPublisher must not be null");
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void entryInserted(MapEvent<String, MapSession> evt) {
	}

	@Override
	public void entryUpdated(MapEvent<String, MapSession> evt) {
	}

	@Override
	public void entryDeleted(MapEvent<String, MapSession> event) {
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

}
