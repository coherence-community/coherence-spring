/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import javax.inject.Inject;

import com.tangosol.util.ValueExtractor;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * Configures the beans to support {@link com.tangosol.util.ValueExtractor}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
public class ExtractorConfiguration {

	private final ExtractorService extractorService;

	/**
	 * Create a {@link ExtractorConfiguration} that will use the specified bean context.
	 * @param extractorService the {@link ExtractorService} to use
	 */
	@Inject
	ExtractorConfiguration(ExtractorService extractorService) {
		this.extractorService = extractorService;
	}

	@Bean
	@Primary
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ValueExtractor<?, ?> getExtractor(InjectionPoint injectionPoint) {
		return this.extractorService.getExtractor(injectionPoint);
	}
}
