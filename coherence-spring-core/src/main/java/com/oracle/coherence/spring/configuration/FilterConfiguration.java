/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.tangosol.util.Filter;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Spring Configuration for defining {@link com.tangosol.util.Filter} beans.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
public class FilterConfiguration {

	/**
	 * The Spring application context.
	 */
	private final ConfigurableApplicationContext ctx;

	private final FilterService filterService;

	/**
	 * Create a {@link FilterConfiguration} that will use the specified bean context.
	 * @param ctx the bean context to use
	 * @param filterService service supporting the creation of Filters
	 */
	FilterConfiguration(ConfigurableApplicationContext ctx, FilterService filterService) {
		this.filterService = filterService;
		this.ctx = ctx;
	}

	/**
	 * Create a {@link Filter} bean based on the annotations present
	 * on an injection point.
	 * @param injectionPoint  the {@link InjectionPoint} to
	 *                        create the {@link Filter} for
	 * @return a {@link Filter} bean based on the annotations present
	 *         on the injection point
	 */
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	@SuppressWarnings({"rawtypes", "unchecked"})
	Filter<?> filter(InjectionPoint injectionPoint) {
		return this.filterService.getFilter(injectionPoint);
	}
}
