/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.annotation.event.ServiceName;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.CacheService;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.PartitionedService;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.EventDispatcher;
import com.tangosol.net.events.partition.PartitionedServiceDispatcher;

/**
 * Abstract base class for all observer-based service interceptors.
 *
 * @param <E> the type of {@link Event} this interceptor accepts
 * @param <T> the enumeration of event types E supports
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public abstract class ServiceEventHandler<E extends Event<T>, T extends Enum<T>>
		extends EventHandler<E, T> {

	protected final String serviceName;

	ServiceEventHandler(MethodEventObserver<E> observer, Class<T> classType) {
		super(observer, classType);

		String service = null;

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof ServiceName) {
				service = ((ServiceName) annotation).value();
			}
		}

		this.serviceName = service;
	}

	@Override
	protected boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
		if (dispatcher instanceof PartitionedServiceDispatcher) {
			PartitionedServiceDispatcher psd = (PartitionedServiceDispatcher) dispatcher;
			ConfigurableCacheFactory ccf = getConfigurableCacheFactory(psd.getService());

			if (ccf == null || scopeName == null || scopeName.equals(ccf.getScopeName())) {
				return this.serviceName == null || this.serviceName.equals(removeScope(psd.getServiceName()));
			}
		}

		return false;
	}

	ConfigurableCacheFactory getConfigurableCacheFactory(PartitionedService service) {
		// a bit of a hack, but it should do the job
		if (service instanceof CacheService) {
			CacheService pc = (CacheService) service;
			return pc.getBackingMapManager().getCacheFactory();
		}
		return null;
	}
}
