/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.annotation.event.Created;
import com.oracle.coherence.spring.annotation.event.Destroyed;
import com.oracle.coherence.spring.annotation.event.Truncated;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;

/**
 * Handler for {@link CacheLifecycleEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CacheLifecycleEventHandler
		extends CacheEventHandler<CacheLifecycleEvent, CacheLifecycleEvent.Type> {

	public CacheLifecycleEventHandler(MethodEventObserver<CacheLifecycleEvent> observer) {
		super(observer, CacheLifecycleEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Created) {
				addType(CacheLifecycleEvent.Type.CREATED);
			}
			else if (annotation instanceof Destroyed) {
				addType(CacheLifecycleEvent.Type.DESTROYED);
			}
			else if (annotation instanceof Truncated) {
				addType(CacheLifecycleEvent.Type.TRUNCATED);
			}
		}
	}
}
