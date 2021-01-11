/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import java.util.List;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gunnar Hillert
 */
public class CoherenceSpringFactoryTests {

	@Test
	void testPresenceOfAutoConfigurationClass() {
		List<String> auoConfigurationClasses = SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, getClass().getClassLoader());
		assertThat(auoConfigurationClasses).contains(CoherenceAutoConfiguration.class.getName());
		assertThat(auoConfigurationClasses).contains(CacheAutoConfiguration.class.getName());
	}
}
