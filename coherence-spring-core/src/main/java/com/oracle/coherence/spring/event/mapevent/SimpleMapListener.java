/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;

import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.util.Filter;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.MapListener;
import com.tangosol.util.comparator.SafeComparator;
import com.tangosol.util.function.Remote;

import org.springframework.util.Assert;

/**
 * {@link MapListener} implementation that dispatches {@code MapEvent}s
 * to {@link CoherenceEventListener}
 * annotated methods.
 *
 * @param <K> the type of the cache key
 * @param <V> the type of the cache value
 * @author Gunnar Hillert
 * @since 3.0
 *
 * @see MapListenerRegistrationBean#registerMapListeners(CacheLifecycleEvent)
 */
public class SimpleMapListener<K, V> implements MapListener<K, V>, Comparable<SimpleMapListener<?, ?>> {

	/**
	 * The wild-card value for cache and service names.
	 */
	public static final String WILD_CARD = "*";

	/**
	 * The event observer for this listener.
	 */
	private final MethodMapListener<K, V> observer;

	/**
	 * The name of the cache to observe map events for. Defaults to {@link #WILD_CARD}
	 */
	private String cacheName = WILD_CARD;

	/**
	 * The name of the cache service owing the cache to observe map events for. Defaults to {@link #WILD_CARD}
	 */
	private String serviceName = WILD_CARD;

	/**
	 * The scope name of the cache factory owning the cache to observer map events for.
	 */
	private String scopeName;

	/**
	 * The types of map event to observe.
	 */
	private final EnumSet<MapEventType> eventTypes = EnumSet.noneOf(MapEventType.class);

	/**
	 * The name of the session if this listener is for a resource
	 * managed by a specific session or {@code null} if this listener
	 * is for a resource in any session.
	 */
	private String session;

	/**
	 * A flag indicating whether to subscribe to lite-events.
	 */
	private boolean liteEvents;

	/**
	 * A flag indicating whether the observer is synchronous.
	 */
	private boolean synchronousEvents;

	/**
	 * An optional {@link Filter} to use to filter observed map events.
	 */
	private Filter<?> filter;

	/**
	 * An optional {@link MapEventTransformer} to use to transform observed map events.
	 */
	private MapEventTransformer<K, V, ?> transformer;

	/**
	 * Constructs a {@code SimpleMapListener}.
	 * @param observer must not be {@code null}
	 */
	public SimpleMapListener(MethodMapListener<K, V> observer) {
		Assert.notNull(observer, "observer must not be null");
		this.observer = observer;
	}

	@Override
	public void entryInserted(MapEvent<K, V> event) {
		handle(MapEventType.INSERTED, event);
	}

	@Override
	public void entryUpdated(MapEvent<K, V> event) {
		handle(MapEventType.UPDATED, event);
	}

	@Override
	public void entryDeleted(MapEvent<K, V> event) {
		handle(MapEventType.DELETED, event);
	}

	@Override
	public int compareTo(SimpleMapListener<?, ?> other) {
		int result = SafeComparator.compareSafe(Remote.Comparator.naturalOrder(), this.session, other.session);
		if (result == 0) {
			result = SafeComparator.compareSafe(Remote.Comparator.naturalOrder(), this.cacheName, other.cacheName);
			if (result == 0) {
				result = SafeComparator.compareSafe(Remote.Comparator.naturalOrder(), this.serviceName, other.serviceName);
			}
		}
		return result;
	}

	/**
	 * Return the name of the session that this listener is for.
	 * @return the name of the session this listener is for
	 */
	public String getSession() {
		return this.session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	/**
	 * Obtain the {@link Filter} that should be used when registering this listener.
	 * @return the {@link Filter} that should be used when registering this listener
	 */
	public Filter<?> getFilter() {
		return this.filter;
	}

	public void setFilter(Filter<?> filter) {
		this.filter = filter;
	}

	/**
	 * Obtain the {@link MapEventTransformer} that should be used when registering this listener.
	 * @return the {@link MapEventTransformer} that should be used when registering this listener
	 */
	@SuppressWarnings("rawtypes")
	public MapEventTransformer getTransformer() {
		return this.transformer;
	}

	public void setTransformer(MapEventTransformer<K, V, ?> transformer) {
		this.transformer = transformer;
	}

	/**
	 * Return the name of the cache this listener is for, or {@code '*'} if
	 * it should be registered regardless of the cache name.
	 * @return the name of the cache this listener is for
	 */
	String getCacheName() {
		return this.cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

	/**
	 * Return {@code true} if this listener is for a wild-card cache name.
	 * @return {@code true} if this listener is for a wild-card cache name
	 */
	boolean isWildCardCacheName() {
		return WILD_CARD.equals(this.cacheName);
	}

	/**
	 * Return the name of the service this listener is for, or {@code '*'} if
	 * it should be registered regardless of the service name.
	 * @return the name of the cache this listener is for
	 */
	String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Return {@code true} if this listener is for a wild-card cache name.
	 * @return {@code true} if this listener is for a wild-card cache name
	 */
	boolean isWildCardServiceName() {
		return WILD_CARD.equals(this.serviceName);
	}

	/**
	 * Return the name of the scope this listener is for, or {@code null} if
	 * it should be registered regardless of the scope name.
	 * @return the name of the cache this listener is for
	 */
	String getScopeName() {
		return this.scopeName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * Return {@code true} if this is lite event listener.
	 * @return {@code true} if this is lite event listener
	 */
	boolean isLiteEvent() {
		return this.liteEvents;
	}

	public void setLiteEvent(boolean liteEvents) {
		this.liteEvents = liteEvents;
	}
	/**
	 * Return {@code true} if this is synchronous event listener.
	 * @return {@code true} if this is synchronous event listener
	 */
	public boolean isSynchronous() {
		return this.synchronousEvents;
	}

	public void setSynchronousEvents(boolean synchronousEvents) {
		this.synchronousEvents = synchronousEvents;
	}

	/**
	 * Add specified event type to a set of types this interceptor should handle.
	 * @param type the event type to add
	 */
	protected void addType(MapEventType type) {
		this.eventTypes.add(type);
	}

	public MethodMapListener<K, V> getObserver() {
		return this.observer;
	}

	/**
	 * Return {@code true} if this listener should handle events of the specified
	 * type.
	 * @param type the type to check
	 * @return {@code true} if this listener should handle events of the specified
	 * type
	 */
	private boolean isSupported(MapEventType type) {
		return this.eventTypes.isEmpty() || this.eventTypes.contains(type);
	}

	/**
	 * Notify the observer that the specified event occurred, if the event type
	 * is supported.
	 * @param type  the event type
	 * @param event the event
	 */
	private void handle(MapEventType type, MapEvent<K, V> event) {
		if (isSupported(type)) {
			if (this.observer.isAsync()) {
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

	@Override
	public String toString() {
		return "SimpleMapListener{" +
				"cacheName='" + this.cacheName + '\'' +
				", serviceName='" + this.serviceName + '\'' +
				", scopeName='" + this.scopeName + '\'' +
				", session='" + this.session + '\'' +
				", observer='" + this.observer + '\'' +
				'}';
	}

}
