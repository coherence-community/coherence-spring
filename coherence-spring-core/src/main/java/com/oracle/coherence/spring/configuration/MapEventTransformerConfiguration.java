/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.ValueExtractor;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Spring Configuration class for producing {@link MapEventTransformer} beans.
 *
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
public class MapEventTransformerConfiguration {

	/**
	 * The Spring application context.
	 */
	private final ConfigurableApplicationContext ctx;

	/**
	 * The extractor factory for use when creating views.
	 */
	protected final ExtractorService extractorService;

	protected final MapEventTransformerService mapEventTransformerService;

	/**
	 * Create a {@link MapEventTransformerConfiguration} instance.
	 * @param ctx the Spring application context
	 * @param extractorService the service to use to produce {@link ValueExtractor ValueExtractors}
	 * @param mapEventTransformerService the service to use to produce {@link MapEventTransformer}s.
	 */
	MapEventTransformerConfiguration(ConfigurableApplicationContext ctx, ExtractorService extractorService,
			MapEventTransformerService mapEventTransformerService) {
		this.ctx = ctx;
		this.extractorService = extractorService;
		this.mapEventTransformerService = mapEventTransformerService;
	}

	/**
	 * Create a {@link MapEventTransformer} for the specified injection point.
	 * @param injectionPoint the injection point to inject a {@link MapEventTransformer} into
	 * @return a {@link MapEventTransformer} for the specified injection point
	 */
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	MapEventTransformer transformer(InjectionPoint injectionPoint) {
		return this.mapEventTransformerService.getMapEventTransformer(injectionPoint);
	}
}
