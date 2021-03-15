/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.FilterBinding;
import com.oracle.coherence.spring.annotation.MapEventTransformerBinding;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.event.CacheName;
import com.oracle.coherence.spring.annotation.event.Deleted;
import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.Lite;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.ScopeName;
import com.oracle.coherence.spring.annotation.event.ServiceName;
import com.oracle.coherence.spring.annotation.event.Synchronous;
import com.oracle.coherence.spring.annotation.event.Updated;
import com.oracle.coherence.spring.configuration.FilterService;
import com.oracle.coherence.spring.configuration.MapEventTransformerService;
import com.tangosol.util.Filter;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.MapListener;
import com.tangosol.util.comparator.SafeComparator;
import com.tangosol.util.function.Remote;

/**
 * {@link MapListener} implementation that dispatches {@code MapEvent}s
 * to {@link CoherenceEventListener}
 * annotated methods.
 *
 * @param <K> the type of the cache key
 * @param <V> the type of the cache value
 * @author Jonathan Knight
 * @since 1.0
 */
class AnnotatedMapListener<K, V> implements MapListener<K, V>, Comparable<AnnotatedMapListener<?, ?>> {

	/**
	 * The wild-card value for cache and service names.
	 */
	public static final String WILD_CARD = "*";

	/**
	 * The event observer for this listener.
	 */
	private final MethodMapListener<K, V> observer;

	/**
	 * The name of the cache to observe map events for.
	 */
	private final String cacheName;

	/**
	 * The name of the cache service owing the cache to observe map events for.
	 */
	private final String serviceName;

	/**
	 * The scope name of the cache factory owning the cache to observer map events for.
	 */
	private final String scopeName;

	/**
	 * The types of map event to observe.
	 */
	private final EnumSet<Type> eventTypes = EnumSet.noneOf(Type.class);

	/**
	 * The optional annotation specifying the filter to use to filter events.
	 */
	private final Set<Annotation> filterAnnotations;

	/**
	 * The optional annotations specifying the map event transformers to use to
	 * transform observed map events.
	 */
	private final Set<Annotation> transformerAnnotations;

	/**
	 * The optional annotations specifying the value extractors to use to
	 * transform observed map events.
	 */
	private final Set<Annotation> extractorAnnotations;

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

	AnnotatedMapListener(MethodMapListener<K, V> observer, Set<Annotation> annotations) {
		this.observer = observer;

		String cacheName = WILD_CARD;
		String serviceName = WILD_CARD;
		String scopeName = null;

		for (Annotation a : observer.getObservedQualifiers()) {
			if (a instanceof CacheName) {
				cacheName = ((CacheName) a).value();
			}
			else if (a instanceof MapName) {
				cacheName = ((MapName) a).value();
			}
			else if (a instanceof ServiceName) {
				serviceName = ((ServiceName) a).value();
			}
			else if (a instanceof ScopeName) {
				scopeName = ((ScopeName) a).value();
			}
			else if (a instanceof Inserted) {
				addType(Type.INSERTED);
			}
			else if (a instanceof Updated) {
				addType(Type.UPDATED);
			}
			else if (a instanceof Deleted) {
				addType(Type.DELETED);
			}
			else if (a instanceof SessionName) {
				this.session = ((SessionName) a).value();
			}
			else if (a instanceof Lite) {
				this.liteEvents = true;
			}
			else if (a instanceof Synchronous) {
				this.synchronousEvents = true;
			}
		}

		this.filterAnnotations = annotations.stream()
				.filter((a) -> a.annotationType().isAnnotationPresent(FilterBinding.class))
				.collect(Collectors.toSet());

		this.extractorAnnotations = annotations.stream()
				.filter((a) -> a.annotationType().isAnnotationPresent(ExtractorBinding.class))
				.collect(Collectors.toSet());

		this.transformerAnnotations = annotations.stream()
				.filter((a) -> a.annotationType().isAnnotationPresent(MapEventTransformerBinding.class))
				.collect(Collectors.toSet());

		this.cacheName = cacheName;
		this.serviceName = serviceName;
		this.scopeName = scopeName;
	}

	@Override
	public void entryInserted(MapEvent<K, V> event) {
		handle(Type.INSERTED, event);
	}

	@Override
	public void entryUpdated(MapEvent<K, V> event) {
		handle(Type.UPDATED, event);
	}

	@Override
	public void entryDeleted(MapEvent<K, V> event) {
		handle(Type.DELETED, event);
	}

	@Override
	public int compareTo(AnnotatedMapListener<?, ?> other) {
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
	String getSessionName() {
		return this.session;
	}

	/**
	 * Returns {@code true} if this listener has a filter annotation to resolve.
	 * @return {@code true} if this listener has a filter annotation to resolve
	 */
	boolean hasFilterAnnotation() {
		return this.filterAnnotations != null && !this.filterAnnotations.isEmpty();
	}

	/**
	 * Resolve this listener's filter annotation into a {@link Filter} instance.
	 * <p>
	 * If this listener's filter has already been resolved this operation is a no-op.
	 * @param filterService the {@link FilterService} to use to resolve the {@link Filter}
	 */
	void resolveFilter(FilterService filterService) {
		if (this.filter == null && this.filterAnnotations != null && !this.filterAnnotations.isEmpty()) {
			this.filter = filterService.resolve(this.filterAnnotations);
		}
	}

	/**
	 * Returns {@code true} if this listener has a transformer annotation to resolve.
	 * @return {@code true} if this listener has a transformer annotation to resolve
	 */
	boolean hasTransformerAnnotation() {
		return !this.transformerAnnotations.isEmpty() || !this.extractorAnnotations.isEmpty();
	}

	/**
	 * Resolve this listener's transformer annotation into a {@link MapEventTransformer} instance.
	 * <p>
	 * If this listener's transformer has already been resolved this method is a no-op
	 * @param producer the {@link MapEventTransformerService} to use to resolve
	 *                 the {@link MapEventTransformer}
	 */
	void resolveTransformer(MapEventTransformerService producer) {
		if (this.transformer != null) {
			return;
		}

		if (!this.transformerAnnotations.isEmpty()) {
			this.transformer = producer.resolve(this.transformerAnnotations);
		}
		else if (!this.extractorAnnotations.isEmpty()) {
			this.transformer = producer.resolve(this.extractorAnnotations);
		}
	}

	/**
	 * Obtain the {@link Filter} that should be used when registering this listener.
	 * @return the {@link Filter} that should be used when registering this listener
	 */
	Filter<?> getFilter() {
		return this.filter;
	}

	/**
	 * Obtain the {@link MapEventTransformer} that should be used when registering this listener.
	 * @return the {@link MapEventTransformer} that should be used when registering this listener
	 */
	@SuppressWarnings("rawtypes")
	MapEventTransformer getTransformer() {
		return this.transformer;
	}

	/**
	 * Return the name of the cache this listener is for, or {@code '*'} if
	 * it should be registered regardless of the cache name.
	 * @return the name of the cache this listener is for
	 */
	String getCacheName() {
		return this.cacheName;
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

	/**
	 * Return {@code true} if this is lite event listener.
	 * @return {@code true} if this is lite event listener
	 */
	boolean isLite() {
		return this.liteEvents;
	}

	/**
	 * Return {@code true} if this is synchronous event listener.
	 * @return {@code true} if this is synchronous event listener
	 */
	boolean isSynchronous() {
		return this.synchronousEvents;
	}

	/**
	 * Add specified event type to a set of types this interceptor should handle.
	 * @param type the event type to add
	 */
	private void addType(Type type) {
		this.eventTypes.add(type);
	}

	/**
	 * Return {@code true} if this listener should handle events of the specified
	 * type.
	 * @param type the type to check
	 * @return {@code true} if this listener should handle events of the specified
	 * type
	 */
	private boolean isSupported(Type type) {
		return this.eventTypes.isEmpty() || this.eventTypes.contains(type);
	}

	/**
	 * Notify the observer that the specified event occurred, if the event type
	 * is supported.
	 * @param type  the event type
	 * @param event the event
	 */
	private void handle(Type type, MapEvent<K, V> event) {
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
		return "AnnotatedMapListener{" +
				"cacheName='" + this.cacheName + '\'' +
				", serviceName='" + this.serviceName + '\'' +
				", scopeName='" + this.scopeName + '\'' +
				", session='" + this.session + '\'' +
				", observer='" + this.observer + '\'' +
				'}';
	}

	/**
	 * Event type enumeration.
	 */
	enum Type {
		INSERTED,
		UPDATED,
		DELETED
	}
}
