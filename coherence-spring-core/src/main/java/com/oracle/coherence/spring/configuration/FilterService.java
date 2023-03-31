/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
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

import com.oracle.coherence.spring.annotation.FilterBinding;
import com.oracle.coherence.spring.annotation.FilterFactory;
import com.oracle.coherence.spring.configuration.support.CoherenceAnnotationUtils;
import com.oracle.coherence.spring.configuration.support.CommonFilterFactories;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

/**
 * Spring Configuration for defining {@link Filter} beans.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class FilterService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilterService.class);

	private final ConfigurableApplicationContext applicationContext;

	private final Map<String, FilterFactory> filterFactories;

	public FilterService(ConfigurableApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.filterFactories = CommonFilterFactories.getFilterFactories();
	}

	private Filter getFilterFromApplicationContext(Annotation coherenceAnnotation) {
		Assert.notNull(coherenceAnnotation, "coherenceAnnotation must not be null.");

		final Class filterFactoryClass = FilterFactory.class;
		final String[] beanNames = this.applicationContext.getBeanNamesForType(filterFactoryClass);
		LOGGER.debug("Found {} beans in the application context for bean type {}", beanNames.length, filterFactoryClass.getName());

		final Collection<FilterFactory> beans = CoherenceAnnotationUtils.getBeansOfTypeWithAnnotation(
				this.applicationContext,
				filterFactoryClass,
				coherenceAnnotation.annotationType());

		final List<Filter> foundFilters = beans.stream()
				.map((filterFactory) -> filterFactory.create(coherenceAnnotation))
				.collect(Collectors.toList());

		if (!foundFilters.isEmpty() && foundFilters.size() == 1) {
			return foundFilters.iterator().next();
		}
		else if (foundFilters.size() > 1) {
			throw new IllegalStateException(String.format("Needed 1 but found %s beans annotated with '%s'",
					foundFilters.size(), coherenceAnnotation.annotationType().getName()));
		}
		return null;
	}

	public Filter<?> getFilter(InjectionPoint injectionPoint) {
		Assert.notNull(injectionPoint, "injectionPoint must not be null.");
		final List<Annotation> annotations = CoherenceAnnotationUtils.getAnnotationsMarkedWithMarkerAnnotation(injectionPoint, FilterBinding.class);
		return this.resolve(annotations);
	}

	/**
	 * Resolve a {@link Filter} implementation from the specified qualifiers.
	 * @param annotations  the qualifiers to use to create the {@link Filter}
	 * @param <T>          the type that the {@link Filter} can filter
	 * @return a {@link Filter} implementation created from the specified qualifiers.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public <T> Filter<T> resolve(Collection<Annotation> annotations) {
		final List<Filter<?>> list = new ArrayList<>();

		for (Annotation annotation : annotations) {
			Filter filter = this.getFilterFromApplicationContext(annotation);
			if (filter != null) {
				list.add(filter);
				continue;
			}

			final Class<? extends Annotation> annotationType = annotation.annotationType();
			final FilterFactory<Annotation, ?> filterFactory = this.filterFactories.get(annotationType.getName());
			if (filterFactory == null) {
				throw new IllegalStateException(String.format("No filterFactory found for annotation %s.", annotationType.getCanonicalName()));
			}
			list.add(filterFactory.create(annotation));
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
