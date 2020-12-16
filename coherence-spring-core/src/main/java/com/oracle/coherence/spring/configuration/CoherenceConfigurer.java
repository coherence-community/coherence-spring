/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.oracle.coherence.spring.CoherenceServer;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;

/**
 * Provides a strategy interface in order to customize the Coherence configuration.
 * Users should typically not directly use getter methods from a {@code CoherenceConfigurer}
 * directly unless they are using it to supply the implementations for Spring Beans.
 *
 * @author Gunnar Hillert
 */
public interface CoherenceConfigurer {

	/**
	 * @return Coherence instance
	 */
	Coherence getCoherence();

	/**
	 * @return The Coherence Server
	 */
	CoherenceServer getCoherenceServer();

	/**
	 * @return The configuration for a Coherence instance
	 */
	CoherenceConfiguration getCoherenceConfiguration();
}
