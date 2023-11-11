/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import java.lang.annotation.Annotation;
import java.util.Set;
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
import com.oracle.coherence.spring.configuration.MapEventTransformerService;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.MapListener;

/**
 * {@link MapListener} implementation that dispatches {@code MapEvent}s
 * to {@link CoherenceEventListener}
 * annotated methods.
 *
 * @param <K> the type of the cache key
 * @param <V> the type of the cache value
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 *
 * @see MapListenerRegistrationBean#registerMapListeners(CacheLifecycleEvent)
 */
public class AnnotatedMapListener<K, V> extends SimpleMapListener<K, V> {

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
	 * Constructs an {@code AnnotatedMapListener}.
	 * @param observer whose annotations are used to set various properties on the listener. Must not be {@code null}.
	 */
	public AnnotatedMapListener(MethodMapListener<K, V> observer) {
		super(observer);

		final Set<Annotation> annotations = observer.getObservedQualifiers();

		for (Annotation annotation : annotations) {
			if (annotation instanceof CacheName) {
				setCacheName(((CacheName) annotation).value());
			}
			else if (annotation instanceof MapName) {
				setCacheName(((MapName) annotation).value());
			}
			else if (annotation instanceof ServiceName) {
				setServiceName(((ServiceName) annotation).value());
			}
			else if (annotation instanceof ScopeName) {
				setScopeName(((ScopeName) annotation).value());
			}
			else if (annotation instanceof Inserted) {
				addType(MapEventType.INSERTED);
			}
			else if (annotation instanceof Updated) {
				addType(MapEventType.UPDATED);
			}
			else if (annotation instanceof Deleted) {
				addType(MapEventType.DELETED);
			}
			else if (annotation instanceof SessionName) {
				setSession(((SessionName) annotation).value());
			}
			else if (annotation instanceof Lite) {
				setLiteEvent(true);
			}
			else if (annotation instanceof Synchronous) {
				setSynchronousEvents(true);
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
	}

	/**
	 * Returns {@code true} if this listener has a filter annotation to resolve.
	 * @return {@code true} if this listener has a filter annotation to resolve
	 */
	boolean hasFilterAnnotation() {
		return this.filterAnnotations != null && !this.filterAnnotations.isEmpty();
	}

	public Set<Annotation> getFilterAnnotations() {
		return this.filterAnnotations;
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
	 * @param mapEventTransformerService the {@link MapEventTransformerService} to use to resolve
	 *                 the {@link MapEventTransformer}
	 */
	void resolveTransformer(MapEventTransformerService mapEventTransformerService) {
		if (this.getTransformer() != null) {
			return;
		}

		if (!this.transformerAnnotations.isEmpty()) {
			this.setTransformer(mapEventTransformerService.resolve(this.transformerAnnotations));
		}
		else if (!this.extractorAnnotations.isEmpty()) {
			this.setTransformer(mapEventTransformerService.resolve(this.extractorAnnotations));
		}
	}

	@Override
	public String toString() {
		return "AnnotatedMapListener{" +
				"cacheName='" + this.getCacheName() + '\'' +
				", serviceName='" + this.getServiceName() + '\'' +
				", scopeName='" + this.getScopeName() + '\'' +
				", session='" + this.getSession() + '\'' +
				", observer='" + this.getObserver() + '\'' +
				'}';
	}

}
