/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.event.CacheName;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.ServiceName;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.EventDispatcher;
import com.tangosol.net.events.partition.cache.CacheLifecycleEventDispatcher;

/**
 * Abstract base class for all observer-based cache interceptors.
 *
 * @param <E> the type of {@link Event} this interceptor accepts
 * @param <T> the enumeration of event types E supports
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public abstract class CacheEventHandler<E extends Event<T>, T extends Enum<T>> extends EventHandler<E, T> {

	protected final String cacheName;

	protected final String serviceName;

	protected final String sessionName;

	CacheEventHandler(MethodEventObserver<E> observer, Class<T> type) {
		super(observer, type);

		String cache = null;
		String service = null;
		String session = null;

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof CacheName) {
				cache = ((CacheName) annotation).value();
			}
			else if (annotation instanceof MapName) {
				cache = ((MapName) annotation).value();
			}
			else if (annotation instanceof ServiceName) {
				service = ((ServiceName) annotation).value();
			}
			else if (annotation instanceof SessionName) {
				session = ((SessionName) annotation).value();
			}
		}

		this.cacheName = cache;
		this.serviceName = service;
		this.sessionName = session;
	}

	@Override
	boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
		if (dispatcher instanceof CacheLifecycleEventDispatcher) {
			CacheLifecycleEventDispatcher cacheDispatcher = (CacheLifecycleEventDispatcher) dispatcher;

			if (scopeName == null || scopeName.equals(cacheDispatcher.getScopeName())) {
				return ((this.cacheName == null || this.cacheName.equals(cacheDispatcher.getCacheName())) &&
						(this.serviceName == null || this.serviceName.equals(removeScope(cacheDispatcher.getServiceName()))));
			}
		}

		return false;
	}
}
