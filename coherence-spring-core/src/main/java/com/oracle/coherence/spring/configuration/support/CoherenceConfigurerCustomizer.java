/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import com.oracle.coherence.spring.configuration.CoherenceConfigurer;
import com.oracle.coherence.spring.configuration.DefaultCoherenceConfigurer;

/**
 * Callback interface that can be used to customize the {@link CoherenceConfigurer}.
 *
 * @param <T> the specific type of the {@link CoherenceConfigurer}
 * @author Gunnar Hillert
 * @since 3.0
 * @see DefaultCoherenceConfigurer
 */
@FunctionalInterface
public interface CoherenceConfigurerCustomizer<T extends CoherenceConfigurer> {

	/**
	 * Callback to customize a {@link CoherenceConfigurer} instance.
	 * @param configurer the coherenceConfigurer to customize
	 */
	void customize(T configurer);

}
