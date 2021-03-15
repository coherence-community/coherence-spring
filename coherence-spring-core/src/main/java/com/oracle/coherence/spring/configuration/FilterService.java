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
import java.util.Set;

import com.oracle.coherence.spring.annotation.FilterBinding;
import com.oracle.coherence.spring.annotation.FilterFactory;
import com.oracle.coherence.spring.configuration.support.CoherenceAnnotationUtils;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Spring Configuration for defining {@link Filter} beans.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class FilterService {

	final ConfigurableApplicationContext applicationContext;

	public FilterService(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Filter<?> getFilter(InjectionPoint injectionPoint) {
		final Annotation annotation = CoherenceAnnotationUtils.getSingleAnnotationMarkedWithMarkerAnnotation(injectionPoint, FilterBinding.class);

		if (annotation != null) {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			final Map<String, Object> beans = this.applicationContext.getBeansWithAnnotation(annotationType);

			if (beans.isEmpty()) {
				throw new IllegalStateException(String.format("No bean annotated with '%s' found.", annotationType.getCanonicalName()));
			}
			else if (beans.size() > 1) {
				throw new IllegalStateException(String.format("Needed 1 but found %s beans annotated with '%s': %s",
						beans.size(), annotationType.getCanonicalName(), StringUtils.collectionToCommaDelimitedString(beans.keySet())));
			}

			@SuppressWarnings({"unchecked", "rawtypes"})
			final FilterFactory<Annotation, Object> filterFactory = (FilterFactory) beans.values().iterator().next();
			return filterFactory.create(annotation);
		}
		else {
			return Filters.always();
		}
	}

	/**
	 * Resolve a {@link Filter} implementation from the specified qualifiers.
	 * @param annotations  the qualifiers to use to create the {@link Filter}
	 * @param <T>          the type that the {@link Filter} can filter
	 * @return a {@link Filter} implementation created from the specified qualifiers.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> Filter<T> resolve(Set<Annotation> annotations) {
		final List<Filter<?>> list = new ArrayList<>();

		for (Annotation annotation : annotations) {
			final Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationType.isAnnotationPresent(FilterBinding.class)) {
				final FilterFactory factory = CoherenceAnnotationUtils.getSingleBeanWithAnnotation(this.applicationContext, annotationType);
				final Filter filter = factory.create(annotation);
				if (filter != null) {
					list.add(filter);
				}
			}
		}

		Filter[] aFilters = list.toArray(new Filter[0]);

		if (aFilters.length == 0) {
			return Filters.always();
		}
		else if (aFilters.length == 1) {
			return aFilters[0];
		}
		else {
			return Filters.all(aFilters);
		}
	}
}
