/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.ExtractorFactory;
import com.oracle.coherence.spring.configuration.support.CoherenceAnnotationUtils;
import com.oracle.coherence.spring.configuration.support.CommonExtractorFactories;
import com.tangosol.util.Extractors;
import com.tangosol.util.ValueExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * Service that supports the {@link ExtractorConfiguration}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class ExtractorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorService.class);

	private final ConfigurableApplicationContext applicationContext;

	private final Map<String, ExtractorFactory> extractorFactories;

	public ExtractorService(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.extractorFactories = CommonExtractorFactories.getExtractorFactories();
	}

	private ValueExtractor getExtractorFromApplicationContext(Annotation coherenceAnnotation) {
		Assert.notNull(coherenceAnnotation, "coherenceAnnotation must not be null.");

		final Class extractorFactoryClass = ExtractorFactory.class;
		final String[] beanNames = this.applicationContext.getBeanNamesForType(ExtractorFactory.class);
		LOGGER.debug("Found {} beans in the application context for bean type {}", beanNames.length, extractorFactoryClass.getName());

		final Collection<ExtractorFactory> beans = CoherenceAnnotationUtils.getBeansOfTypeWithAnnotation(
				this.applicationContext,
				extractorFactoryClass,
				coherenceAnnotation.annotationType());

		final List<ValueExtractor> foundExtractors = beans.stream()
				.map((extractorFactory) -> extractorFactory.create(coherenceAnnotation))
				.collect(Collectors.toList());

		if (!foundExtractors.isEmpty() && foundExtractors.size() == 1) {
			return foundExtractors.iterator().next();
		}
		else if (foundExtractors.size() > 1) {
			throw new IllegalStateException(String.format("Needed 1 but found %s beans annotated with '%s'",
					foundExtractors.size(), coherenceAnnotation.annotationType().getName()));
		}
		return null;
	}

	/**
	 * Create a {@link ValueExtractor} bean based on the annotations present on an injection point.
	 * @param injectionPoint the {@link InjectionPoint} to create the {@link ValueExtractor} for
	 * @param returnNullIfNotFound if true return null if no {@link ValueExtractor} was found on the {@link InjectionPoint}
	 *                             otherwise throw an IllegalStateException
	 * @return a {@link ValueExtractor} bean based on the annotations present
	 * on the injection point
	 */
	ValueExtractor<?, ?> getExtractor(InjectionPoint injectionPoint, boolean returnNullIfNotFound) {
		Assert.notNull(injectionPoint, "injectionPoint must not be null.");
		final List<Annotation> annotations = CoherenceAnnotationUtils.getAnnotationsMarkedWithMarkerAnnotation(injectionPoint, ExtractorBinding.class);
		return this.resolve(annotations);
	}

	/**
	 * Create a {@link ValueExtractor} bean based on the annotations present on an injection point.
	 * @param injectionPoint the {@link InjectionPoint} to create the {@link ValueExtractor} for
	 * @return a {@link ValueExtractor} bean based on the annotations present on the injection point. Never returns null.
	 * @throws IllegalStateException if no value extractor was found.
	 */
	ValueExtractor<?, ?> getExtractor(InjectionPoint injectionPoint) {
		return this.getExtractor(injectionPoint, false);
	}

	/**
	 * Resolve a {@link ValueExtractor} implementation from the specified qualifiers.
	 * @param annotations  the qualifiers to use to create the {@link ValueExtractor}
	 * @param <T>          the type that the {@link ValueExtractor} can extract from
	 * @param <E>          the type that the {@link ValueExtractor} extracts
	 * @return a {@link ValueExtractor} implementation created from the specified qualifiers.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T, E> ValueExtractor<T, E> resolve(Collection<Annotation> annotations) {
		final List<ValueExtractor> list = new ArrayList<>();

		for (Annotation annotation : annotations) {
			ValueExtractor extractor = this.getExtractorFromApplicationContext(annotation);
			if (extractor != null) {
				list.add(extractor);
				continue;
			}

			final Class<? extends Annotation> annotationType = annotation.annotationType();
			final ExtractorFactory filterFactory = this.extractorFactories.get(annotationType.getName());
			if (filterFactory == null) {
				throw new IllegalStateException(String.format("No filterFactory found for annotation %s.", annotationType.getCanonicalName()));
			}
			list.add(filterFactory.create(annotation));
		}

		ValueExtractor[] aExtractors = list.toArray(new ValueExtractor[0]);
		if (aExtractors.length == 0) {
			return null;
		}
		else if (aExtractors.length == 1) {
			return aExtractors[0];
		}
		else {
			return Extractors.multi(aExtractors);
		}
	}
}
