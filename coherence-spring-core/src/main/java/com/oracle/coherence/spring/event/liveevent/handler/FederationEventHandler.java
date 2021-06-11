/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Function;

import com.oracle.coherence.spring.annotation.event.ParticipantName;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.EventDispatcher;

/**
 * Abstract base class for all observer-based federation interceptors.
 *
 * @param <E> the type of {@link Event} this interceptor accepts
 * @param <T> the enumeration of event types E supports
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public abstract class FederationEventHandler<E extends Event<T>, T extends Enum<T>> extends ServiceEventHandler<E, T> {

	protected final String participantName;
	protected final Function<E, String> participantNameFunction;

	public FederationEventHandler(MethodEventObserver<E> observer, Class<T> type, Function<E, String> participantNameFunction) {
		super(observer, type);

		this.participantNameFunction = participantNameFunction;

		String participantName = null;

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof ParticipantName) {
				participantName = ((ParticipantName) annotation).value();
			}
		}

		this.participantName = participantName;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected boolean isApplicable(EventDispatcher dispatcher, String sScopeName) {
		Set<Enum> setSupported = dispatcher.getSupportedTypes();
		boolean fMatch = eventTypes().stream().anyMatch(setSupported::contains);
		return fMatch && super.isApplicable(dispatcher, sScopeName);
	}

	@Override
	protected boolean shouldFire(E event) {
		return this.participantName == null || this.participantName.equals(this.participantNameFunction.apply(event));
	}
}
