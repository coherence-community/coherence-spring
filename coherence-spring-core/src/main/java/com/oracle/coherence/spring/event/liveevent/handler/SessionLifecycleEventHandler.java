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
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.event.Started;
import com.oracle.coherence.spring.annotation.event.Starting;
import com.oracle.coherence.spring.annotation.event.Stopped;
import com.oracle.coherence.spring.annotation.event.Stopping;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.EventDispatcher;
import com.tangosol.net.events.SessionLifecycleEvent;
import com.tangosol.net.events.internal.SessionEventDispatcher;

/**
 * Handler for {@link SessionLifecycleEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class SessionLifecycleEventHandler extends EventHandler<SessionLifecycleEvent, SessionLifecycleEvent.Type> {

	private String name;

	private static final Set<SessionLifecycleEvent.Type> PRE_EVENT_TYPES = Set.of(
			SessionLifecycleEvent.Type.STARTING,
			SessionLifecycleEvent.Type.STOPPING);

	public SessionLifecycleEventHandler(MethodEventObserver<SessionLifecycleEvent> observer) {
		super(observer, SessionLifecycleEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Starting) {
				addType(SessionLifecycleEvent.Type.STARTING);
			}
			else if (annotation instanceof Started) {
				addType(SessionLifecycleEvent.Type.STARTED);
			}
			else if (annotation instanceof Stopping) {
				addType(SessionLifecycleEvent.Type.STOPPING);
			}
			else if (annotation instanceof Stopped) {
				addType(SessionLifecycleEvent.Type.STOPPED);
			}
			else if (annotation instanceof Name) {
				this.name = ((Name) annotation).value();
			}
			else if (annotation instanceof SessionName) {
				this.name = ((SessionName) annotation).value();
			}
		}
	}

	@Override
	protected boolean isApplicable(EventDispatcher dispatcher, String scopeName) {
		return dispatcher instanceof SessionEventDispatcher
				&& (this.name == null || ((SessionEventDispatcher) dispatcher).getName().equals(this.name));
	}

	@Override
	boolean isPreEvent(SessionLifecycleEvent event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
