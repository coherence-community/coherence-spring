/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.event.Started;
import com.oracle.coherence.spring.annotation.event.Starting;
import com.oracle.coherence.spring.annotation.event.Stopped;
import com.oracle.coherence.spring.annotation.event.Stopping;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.events.EventDispatcher;
import com.tangosol.net.events.internal.CoherenceEventDispatcher;

/**
 * Handler for {@link CoherenceLifecycleEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceLifecycleEventHandler
		extends EventHandler<CoherenceLifecycleEvent, CoherenceLifecycleEvent.Type> {

	private String name;

	private static final Set<CoherenceLifecycleEvent.Type> PRE_EVENT_TYPES = Set.of(
			CoherenceLifecycleEvent.Type.STARTING,
			CoherenceLifecycleEvent.Type.STOPPING);

	public CoherenceLifecycleEventHandler(MethodEventObserver<CoherenceLifecycleEvent> observer) {
		super(observer, CoherenceLifecycleEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Starting) {
				addType(CoherenceLifecycleEvent.Type.STARTING);
			}
			else if (annotation instanceof Started) {
				addType(CoherenceLifecycleEvent.Type.STARTED);
			}
			else if (annotation instanceof Stopping) {
				addType(CoherenceLifecycleEvent.Type.STOPPING);
			}
			else if (annotation instanceof Stopped) {
				addType(CoherenceLifecycleEvent.Type.STOPPED);
			}
			else if (annotation instanceof Name) {
				this.name = ((Name) annotation).value();
			}
		}
	}

	@Override
	protected boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
		return dispatcher instanceof CoherenceEventDispatcher
				&& (this.name == null || ((CoherenceEventDispatcher) dispatcher).getName().equals(this.name));
	}

	@Override
	boolean isPreEvent(CoherenceLifecycleEvent event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
