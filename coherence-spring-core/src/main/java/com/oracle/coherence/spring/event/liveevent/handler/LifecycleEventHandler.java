/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.oracle.coherence.spring.annotation.event.Activated;
import com.oracle.coherence.spring.annotation.event.Activating;
import com.oracle.coherence.spring.annotation.event.Disposing;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.EventDispatcher;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.internal.ConfigurableCacheFactoryDispatcher;

/**
 * Handler for {@link LifecycleEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class LifecycleEventHandler extends EventHandler<LifecycleEvent, LifecycleEvent.Type> {

	private static final Set<LifecycleEvent.Type> PRE_EVENT_TYPES = Set.of(
			LifecycleEvent.Type.ACTIVATING,
			LifecycleEvent.Type.DISPOSING);

	public LifecycleEventHandler(MethodEventObserver<LifecycleEvent> observer) {
		super(observer, LifecycleEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Activating) {
				addType(LifecycleEvent.Type.ACTIVATING);
			}
			else if (annotation instanceof Activated) {
				addType(LifecycleEvent.Type.ACTIVATED);
			}
			else if (annotation instanceof Disposing) {
				addType(LifecycleEvent.Type.DISPOSING);
			}
		}
	}

	@Override
	boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
		return dispatcher instanceof ConfigurableCacheFactoryDispatcher;
	}

	@Override
	String getEventScope(LifecycleEvent event) {
		return event.getConfigurableCacheFactory().getScopeName();
	}

	@Override
	boolean isPreEvent(LifecycleEvent event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
