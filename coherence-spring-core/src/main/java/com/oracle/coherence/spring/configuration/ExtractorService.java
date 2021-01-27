/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.ExtractorFactory;
import com.oracle.coherence.spring.configuration.support.CoherenceAnnotationUtils;
import com.tangosol.util.Extractors;
import com.tangosol.util.ValueExtractor;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Service that supports the {@link ExtractorConfiguration}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class ExtractorService {

	final ConfigurableApplicationContext applicationContext;

	public ExtractorService(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
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

		final List<Annotation> extractorAnnotations = CoherenceAnnotationUtils.getAnnotationsMarkedWithMarkerAnnotation(injectionPoint, ExtractorBinding.class);

		final List<ValueExtractor<?, ?>> valueExtractors = new ArrayList<>();

		if (!CollectionUtils.isEmpty(extractorAnnotations)) {
			for (Annotation annotation : extractorAnnotations) {
				final Class<? extends Annotation> annotationType = annotation.annotationType();
				final Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(annotationType);

				if (beans.isEmpty()) {
					throw new IllegalStateException(String.format("No bean annotated with '%s' found.", annotationType.getCanonicalName()));
				}
				else if (beans.size() > 1) {
					throw new IllegalStateException(String.format("Needed 1 but found %s beans annotated with '%s': %s.",
							beans.size(), annotationType.getCanonicalName(), StringUtils.collectionToCommaDelimitedString(beans.keySet())));
				}
				@SuppressWarnings({"unchecked", "rawtypes"})
				final ExtractorFactory<Annotation, Object, Object> extractorFactory = (ExtractorFactory) beans.values().iterator().next();
				valueExtractors.add(extractorFactory.create(annotation));
			}
		}

		@SuppressWarnings("unchecked")
		final ValueExtractor<Object, Object>[] valueExtractorsAsArray = valueExtractors.toArray(new ValueExtractor[0]);
		if (valueExtractorsAsArray.length == 0) {
			if (returnNullIfNotFound) {
				return null;
			}
			else {
				throw new IllegalStateException("Unsatisfied dependency - no ExtractorFactory bean found annotated with "); // + bindings);
			}
		}
		else if (valueExtractorsAsArray.length == 1) {
			return valueExtractorsAsArray[0];
		}
		else {
			return Extractors.multi(valueExtractorsAsArray);
		}
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
}
