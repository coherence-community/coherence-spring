/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

import com.oracle.coherence.spring.annotation.event.ScopeName;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.EventDispatcher;
import com.tangosol.net.events.EventDispatcherAwareInterceptor;

/**
 * Abstract base class for all observer-based interceptors.
 *
 * @param <E> the type of {@link Event} this interceptor accepts
 * @param <T> the enumeration of event types E supports
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public abstract class EventHandler<E extends Event<T>, T extends Enum<T>>
		implements EventDispatcherAwareInterceptor<E> {

	/**
	 * The observer method to delegate events to.
	 */
	protected final MethodEventObserver<E> observer;

	/**
	 * A set of event types the observer is interested in.
	 */
	protected final EnumSet<T> eventTypes;

	/**
	 * The scope name for a {@link ConfigurableCacheFactory} this
	 * interceptor is interested in.
	 */
	private final String scopeName;

	/**
	 * Construct {@code EventHandler} instance.
	 * @param observer       the observer method to delegate events to
	 * @param classEventType the class of event type enumeration
	 */
	EventHandler(MethodEventObserver<E> observer, Class<T> classEventType) {
		this.observer = observer;
		this.eventTypes = EnumSet.noneOf(classEventType);

		String sScope = null;

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof ScopeName) {
				sScope = ((ScopeName) annotation).value();
			}
		}

		this.scopeName = sScope;
	}

	@Override
	public void introduceEventDispatcher(String identifier, EventDispatcher dispatcher) {
		if (isApplicable(dispatcher, this.scopeName)) {
			dispatcher.addEventInterceptor(getId(), this, getEventTypes(), false);
		}
	}

	@Override
	public void onEvent(E event) {
		if (shouldFire(event)) {
			String observerScope = this.scopeName;
			String eventScope = getEventScope(event);

			if (observerScope == null || eventScope == null || observerScope.equals(eventScope)) {
				if (this.observer.isAsync() && !isPreEvent(event)) {
					CompletableFuture.supplyAsync(() -> {
						this.observer.notify(event);
						return event;
					});
				}
				else {
					this.observer.notify(event);
				}
			}
		}
	}

	/**
	 * Return {@code true} if passed event is pre-event (pre-events
	 * are emitted synchronously before the entry is mutated).
	 * @param event the Event to be checked
	 * @return {@code true} if passed event is pre-event;
	 * {@code false} otherwise
	 */
	boolean isPreEvent(E event) {
		return false;
	}

	/**
	 * Return a unique identifier for this interceptor.
	 * @return a unique identifier for this interceptor
	 */
	public String getId() {
		return this.observer.getId();
	}

	/**
	 * Return {@code true} if this interceptor should be registered with
	 * a specified dispatcher.
	 * @param dispatcher a dispatcher to register this interceptor with
	 * @param scopeName  a scope name the observer is interested in,
	 *                   or {@code null} for all scopes
	 * @return {@code true} if this interceptor should be registered with
	 * a specified dispatcher; {@code false} otherwise
	 */
	abstract boolean isApplicable(EventDispatcher dispatcher, String scopeName);

	/**
	 * Return {@code true} if the event should fire.
	 * <p>
	 * This allows sub-classes to provide additional filtering logic and
	 * prevent the observer method notification from happening even after
	 * the Coherence server-side event is fired.
	 * @param event the event to check
	 * @return {@code true} if the event should fire
	 */
	boolean shouldFire(E event) {
		return true;
	}

	/**
	 * Return the scope name of the {@link ConfigurableCacheFactory} the
	 * specified event was raised from.
	 * @param event the event to extract scope name from
	 * @return the scope name
	 */
	String getEventScope(E event) {
		return null;
	}

	/**
	 * Add specified event type to a set of types this interceptor should handle.
	 * @param type the event type to add
	 */
	void addType(T type) {
		this.eventTypes.add(type);
	}

	/**
	 * Create a final set of event types to register this interceptor for.
	 * @return a final set of event types to register this interceptor for
	 */
	protected EnumSet<T> getEventTypes() {
		return this.eventTypes.isEmpty() ? EnumSet.complementOf(this.eventTypes) : this.eventTypes;
	}

	/**
	 * Return the name of the scope this interceptor should be registered with.
	 * @return the name of the scope this interceptor should be registered with
	 */
	String getScopeName() {
		return this.scopeName;
	}

	/**
	 * Remove the scope prefix from a specified service name.
	 * @param serviceName the service name to remove scope prefix from
	 * @return service name with scope prefix removed
	 */
	protected String removeScope(String serviceName) {
		int nIndex = serviceName.indexOf(':');
		return (nIndex > -1) ? serviceName.substring(nIndex + 1) : serviceName;
	}
}
