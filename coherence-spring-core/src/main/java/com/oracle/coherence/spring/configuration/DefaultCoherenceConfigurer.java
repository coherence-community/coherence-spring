/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import com.oracle.coherence.spring.CoherenceServer;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.SessionConfiguration;

/**
 * @author Gunnar Hillert
 *
 */
@Configuration
public class DefaultCoherenceConfigurer implements CoherenceConfigurer {

	private static final Log logger = LogFactory.getLog(DefaultCoherenceConfigurer.class);

	private Collection<SessionConfiguration> sessionConfigurations;

	private Collection<Coherence.LifecycleListener> lifecycleListeners;

	private CoherenceConfiguration coherenceConfiguration;

	private Coherence coherence;

	private CoherenceServer coherenceServer;

	private ConfigurableApplicationContext context;

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

		if (this.context != null) {
			final Collection<SessionConfiguration> sessionConfigurations =
					context.getBeansOfType(SessionConfiguration.class).values();

			if (!CollectionUtils.isEmpty(sessionConfigurations)) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("% sessionConfiguration(s) found.", sessionConfigurations.size()));
				}
				this.sessionConfigurations = sessionConfigurations;
			}

			final Collection<Coherence.LifecycleListener> lifecycleListeners =
					context.getBeansOfType(Coherence.LifecycleListener.class).values();

			if (!CollectionUtils.isEmpty(lifecycleListeners)) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("% lifecycleListener(s) found.", lifecycleListeners.size()));
				}
				this.lifecycleListeners = lifecycleListeners;
			}
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

	/**
	 *
	 * @return
	 */
	protected CoherenceConfiguration createCoherenceConfiguration() {
		final CoherenceConfiguration.Builder builder = CoherenceConfiguration.builder();

		if (!CollectionUtils.isEmpty(this.sessionConfigurations)) {
			builder.withSessions(this.sessionConfigurations);
		}

		if (!CollectionUtils.isEmpty(this.lifecycleListeners)) {
			builder.withEventInterceptors(lifecycleListeners);
		}

		return builder.build();
	}

	protected Coherence createCoherence() {
		return Coherence.clusterMember(this.getCoherenceConfiguration());
	}

	protected CoherenceServer createCoherenceServer() {
		return new CoherenceServer(this.getCoherence());
	}
}
