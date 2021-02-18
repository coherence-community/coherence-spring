/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import java.util.List;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.config.CoherenceConfigDataLoader;
import com.oracle.coherence.spring.boot.config.CoherenceConfigDataLocationResolver;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gunnar Hillert
 */
public class CoherenceSpringFactoryTests {

	@Test
	void testPresenceOfAutoConfigurationClass() {
		final List<String> autoConfigurationClasses = SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, getClass().getClassLoader());
		assertThat(autoConfigurationClasses).contains(CoherenceAutoConfiguration.class.getName());
		assertThat(autoConfigurationClasses).contains(CacheAutoConfiguration.class.getName());
	}

	@Test
	void testPresenceOfConfigDataClasses() {
		final List<String> configDataResolverClasses = SpringFactoriesLoader.loadFactoryNames(ConfigDataLocationResolver.class, getClass().getClassLoader());
		final List<String> configDataLoaderClasses = SpringFactoriesLoader.loadFactoryNames(ConfigDataLoader.class, getClass().getClassLoader());

		assertThat(configDataResolverClasses).contains(CoherenceConfigDataLocationResolver.class.getName());
		assertThat(configDataLoaderClasses).contains(CoherenceConfigDataLoader.class.getName());
	}
}
