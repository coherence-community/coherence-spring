/*
 * File: CoherenceConfigurer.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
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
