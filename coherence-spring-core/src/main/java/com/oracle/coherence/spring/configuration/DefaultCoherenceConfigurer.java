/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
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
