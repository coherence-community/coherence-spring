/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.events;

import java.util.Arrays;
import java.util.HashSet;

import com.tangosol.net.events.EventDispatcher;
import com.tangosol.net.events.EventDispatcherAwareInterceptor;
import com.tangosol.net.events.partition.cache.CacheLifecycleEventDispatcher;
import com.tangosol.net.events.partition.cache.EntryEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.session.MapSession;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.util.Assert;

/**
 * Custom Event that handles the creation of a session. The handled event is then propagated as a Spring application
 * event via Spring's {@link ApplicationEventPublisher}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceSessionCreatedEventHandler implements EventDispatcherAwareInterceptor<EntryEvent<String, MapSession>> {

	private static final Log logger = LogFactory.getLog(CoherenceSessionCreatedEventHandler.class);

	private final ApplicationEventPublisher eventPublisher;

	private String cacheName;
	private String serviceName;
	private String sessionName;
	private String scopeName;

	public CoherenceSessionCreatedEventHandler(ApplicationEventPublisher eventPublisher) {
		Assert.notNull(eventPublisher, "eventPublisher must not be null");
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void introduceEventDispatcher(String sIdentifier, EventDispatcher dispatcher) {
		if (isApplicable(dispatcher, this.scopeName)) {
			dispatcher.addEventInterceptor(sIdentifier, this,
					new HashSet<>(Arrays.asList(EntryEvent.Type.INSERTING)), true);
		}
	}

	@Override
	public void onEvent(EntryEvent<String, MapSession> event) {
		MapSession session = event.getValue();
		if (session.getId().equals(session.getOriginalId())) {
			if (logger.isDebugEnabled()) {
				logger.debug("Session created with id: " + session.getId());
			}
			this.eventPublisher.publishEvent(new SessionCreatedEvent(this, session));
		}
	}

	private boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
		if (dispatcher instanceof CacheLifecycleEventDispatcher) {
			final CacheLifecycleEventDispatcher cacheDispatcher = (CacheLifecycleEventDispatcher) dispatcher;

			if (scopeName == null || scopeName.equals(cacheDispatcher.getScopeName())) {
				return ((this.cacheName == null || this.cacheName.equals(cacheDispatcher.getCacheName())) &&
						(this.serviceName == null || this.serviceName.equals(removeScope(cacheDispatcher.getServiceName()))));
			}
		}

		return false;
	}

	/**
	 * Remove the scope prefix from a specified service name.
	 * @param serviceName the service name to remove scope prefix from
	 * @return service name with scope prefix removed
	 */
	protected String removeScope(String serviceName) {
		int index = serviceName.indexOf(':');
		return (index > -1) ? serviceName.substring(index + 1) : serviceName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
}
