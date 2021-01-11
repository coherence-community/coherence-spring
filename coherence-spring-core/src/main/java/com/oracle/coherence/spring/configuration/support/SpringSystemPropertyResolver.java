/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import com.tangosol.coherence.config.EnvironmentVariableResolver;
import com.tangosol.coherence.config.SystemPropertyResolver;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * A Coherence {@link SystemPropertyResolver} and {@link EnvironmentVariableResolver} that uses the Spring
 * {@link Environment} to obtain values.
 * <p>
 * This class needs to be eagerly instantiated by Spring before any Coherence class that might need properties.
 *
 * @author Gunnar Hillert
 *
 */
public class SpringSystemPropertyResolver
		implements SystemPropertyResolver, EnvironmentVariableResolver {

	/**
	 * The Spring {@link Environment}.
	 */
	private static volatile Environment env;

	/**
	 * An optional prefix. By default not used.
	 */
	private static String propertyPrefix = "";

	/**
	 * This constructor is required so that Coherence can discover
	 * and instantiate this class using the Java ServiceLoader.
	 */
	public SpringSystemPropertyResolver() {
	}

	/**
	 * This constructor will be called by Spring to instantiate the
	 * singleton bean and set the {@link Environment}.
	 * @param environment the Spring {@link Environment}. Must not be null.
	 */
	public SpringSystemPropertyResolver(Environment environment) {
		Assert.notNull(environment, "environment must not be null.");
		SpringSystemPropertyResolver.env = environment;
	}

	/**
	 * This constructor will be called by Spring to instantiate the
	 * singleton bean and set the {@link Environment}.
	 * @param environment the Spring {@link Environment}
	 * @param propertyPrefix must not be null or empty
	 */
	public SpringSystemPropertyResolver(Environment environment, String propertyPrefix) {
		Assert.notNull(environment, "environment must not be null.");
		Assert.notNull(propertyPrefix, "propertyPrefix must not be null or empty.");

		SpringSystemPropertyResolver.env = environment;
		SpringSystemPropertyResolver.propertyPrefix = propertyPrefix;
	}

	@Override
	public String getProperty(String coherenceProperty) {
		return (SpringSystemPropertyResolver.env != null)
				? SpringSystemPropertyResolver.env.getProperty(propertyPrefix + coherenceProperty, String.class)
				: null;
	}

	@Override
	public String getEnv(String coherenceProperty) {
		return (SpringSystemPropertyResolver.env != null)
				? SpringSystemPropertyResolver.env.getProperty(propertyPrefix + coherenceProperty, String.class)
				: null;
	}
}
