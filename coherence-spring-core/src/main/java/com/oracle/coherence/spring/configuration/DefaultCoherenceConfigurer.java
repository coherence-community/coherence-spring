/*
 * File: DefaultCoherenceConfigurer.java
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

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Configuration;

import com.oracle.coherence.spring.CoherenceServer;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;

/**
 * @author Gunnar Hillert
 *
 */
@Configuration
public class DefaultCoherenceConfigurer implements CoherenceConfigurer {

	private static final Log logger = LogFactory.getLog(DefaultCoherenceConfigurer.class);

	private CoherenceConfiguration coherenceConfiguration;

	private Coherence coherence;

	private CoherenceServer coherenceServer;

	private boolean initialized = false;

	@Override
	public Coherence getCoherence() {
		return this.coherence;
	}

	@Override
	public CoherenceServer getCoherenceServer() {
		return this.coherenceServer;
	}

	@Override
	public CoherenceConfiguration getCoherenceConfiguration() {
		return this.coherenceConfiguration;
	}

	@PostConstruct
	public void initialize() {
		if (this.initialized) {
			return;
		}

		if(this.coherenceConfiguration == null) {
			logger.warn("No Coherence configuration was provided...using default.");
			this.coherenceConfiguration = this.createCoherenceConfiguration();
		}
		if(this.coherence == null) {
			logger.warn("No Coherence instance was provided...creating a default instance.");
			this.coherence = this.createCoherence();
		}
		if(this.coherenceServer == null) {
			logger.warn("No Coherence server defined...creating default server.");
			this.coherenceServer = this.createCoherenceServer();
		}
		this.initialized = true;
	}

	protected CoherenceConfiguration createCoherenceConfiguration() {
		CoherenceConfiguration cfg = CoherenceConfiguration.builder()
//				.withSessions(configurations)
//				.withSessionProviders(configProvider)
//				.withEventInterceptors(listenerProcessor.getInterceptors())
				.build();
		return cfg;
	}

	protected Coherence createCoherence() {
		return Coherence.create(this.getCoherenceConfiguration());
	}

	protected CoherenceServer createCoherenceServer() {
		return new CoherenceServer(this.getCoherence());
	}
}
