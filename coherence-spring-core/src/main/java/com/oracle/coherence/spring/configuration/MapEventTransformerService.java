/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.MapEventTransformerBinding;
import com.oracle.coherence.spring.annotation.MapEventTransformerFactory;
import com.oracle.coherence.spring.configuration.support.CoherenceAnnotationUtils;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.transformer.ExtractorEventTransformer;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A service for producing {@link MapEventTransformer} instances.
 *
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
public class MapEventTransformerService {

	/**
	 * The Spring application context.
	 */
	protected final ConfigurableApplicationContext applicationContext;

	/**
	 * The extractor factory for use when creating views.
	 */
	protected final ExtractorService extractorFactory;

	/**
	 * Create a {@link MapEventTransformerService}.
	 * @param applicationContext the Spring application context
	 * @param extractorFactory  the factory to use to produce {@link ValueExtractor ValueExtractors}
	 */
	MapEventTransformerService(ConfigurableApplicationContext applicationContext, ExtractorService extractorFactory) {
		this.applicationContext = applicationContext;
		this.extractorFactory = extractorFactory;
	}

	/**
	 * Resolve a {@link MapEventTransformer} from the
	 * specified qualifier annotations.
	 * @param annotations  the qualifier annotations to use to create the transformer
	 * @param <K>          the type of the keys of the entry to be transformed
	 * @param <V>          the type of the values of the entry to be transformed
	 * @param <U>          the type of the transformed values
	 * @return a {@link MapEventTransformer} from the specified qualifier annotations
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <K, V, U> MapEventTransformer<K, V, U> resolve(Set<Annotation> annotations) {
		Optional<Annotation> optionalTransformer = annotations.stream()
				.filter((annotation) -> annotation.annotationType().isAnnotationPresent(MapEventTransformerBinding.class))
				.findFirst();
		Optional<Annotation> optionalExtractor = annotations.stream()
				.filter((annotation) -> annotation.annotationType().isAnnotationPresent(ExtractorBinding.class))
				.findFirst();

		if (optionalTransformer.isPresent()) {
			final Annotation annotation = optionalTransformer.get();
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			final MapEventTransformerFactory factory =
					CoherenceAnnotationUtils.getSingleBeanWithAnnotation(this.applicationContext, annotationType);
			return factory.create(annotation);

		}
		else if (optionalExtractor.isPresent()) {
			 // there is one or more ExtractorBinding annotations
			 ValueExtractor<Object, Object> extractor = this.extractorFactory.resolve(annotations);
			 return new ExtractorEventTransformer(extractor);
		}

		// there are no transformer or extractor annotations.
		return null;
	}

	/**
	 * Create a {@link MapEventTransformer} for the specified injection point.
	 * @param injectionPoint  the injection point to inject a {@link MapEventTransformer} into
	 * @return a {@link MapEventTransformer} for the specified injection point
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	MapEventTransformer getMapEventTransformer(InjectionPoint injectionPoint) {
	   final  List<Annotation> bindings = CoherenceAnnotationUtils.getAnnotationsMarkedWithMarkerAnnotation(injectionPoint, MapEventTransformerBinding.class);

		for (Annotation annotation : bindings) {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			final MapEventTransformerFactory factory =
					CoherenceAnnotationUtils.getSingleBeanWithAnnotation(this.applicationContext, annotationType);
			return factory.create(annotation);
		}

		throw new IllegalStateException(
				"Unsatisfied dependency - no MapEventTransformerFactory bean found for bindings " + bindings);
	}
}
