/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.configuration.session.AbstractSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.oracle.coherence.spring.configuration.support.CoherenceInstanceType;
import com.oracle.coherence.spring.event.CoherenceEventListenerCandidates;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.SessionConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Default implementation of the {@link CoherenceConfigurer} interface.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
public class DefaultCoherenceConfigurer implements CoherenceConfigurer {

	private static final Log logger = LogFactory.getLog(DefaultCoherenceConfigurer.class);

	private final Collection<SessionConfiguration> sessionConfigurations = new ArrayList<>(0);

	private Collection<Coherence.LifecycleListener> lifecycleListeners;

	private CoherenceConfiguration coherenceConfiguration;

	private Coherence coherence;

	private CoherenceServer coherenceServer;

	private final ConfigurableApplicationContext applicationContext;

	private boolean initialized = false;

	private CoherenceEventListenerCandidates coherenceEventListenerCandidates;

	private Duration coherenceServerStartupTimeout;

	private CoherenceInstanceType coherenceInstanceType;

	public DefaultCoherenceConfigurer(ConfigurableApplicationContext context,
			CoherenceEventListenerCandidates coherenceEventListenerCandidates) {
		this.applicationContext = context;
		this.coherenceEventListenerCandidates = coherenceEventListenerCandidates;
	}

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

		final Set<SessionType> detectedSessionTypes = new HashSet<>();

		if (this.applicationContext != null) {
			final Collection<AbstractSessionConfigurationBean> sessionConfigurationBeans =
					this.applicationContext.getBeansOfType(AbstractSessionConfigurationBean.class).values();

			if (!CollectionUtils.isEmpty(sessionConfigurationBeans)) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("%s sessionConfigurationBean(s) found.", sessionConfigurationBeans.size()));
				}
				this.sessionConfigurations.addAll(
					sessionConfigurationBeans
						.stream()
						.map((sp) -> {
							final SessionType sessionType = sp.getType();
							if (sessionType == null) {
								detectedSessionTypes.add(SessionType.SERVER);
							}
							else {
								detectedSessionTypes.add(sp.getType());
							}
							return sp.getConfiguration().orElseThrow(
									() -> new IllegalStateException("Empty sessionConfigurations found."));

						})
						.collect(Collectors.toList()));
			}

			final Collection<Coherence.LifecycleListener> lifecycleListeners =
					this.applicationContext.getBeansOfType(Coherence.LifecycleListener.class).values();

			if (!CollectionUtils.isEmpty(lifecycleListeners)) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("%s lifecycleListener(s) found.", lifecycleListeners.size()));
				}
				this.lifecycleListeners = lifecycleListeners;
			}
		}

		//TODO
		if (this.getCoherenceConfiguration() == null) {
			if (logger.isInfoEnabled()) {
				logger.info("No Coherence configuration was provided...using default.");
			}
			this.coherenceConfiguration = this.createCoherenceConfiguration();
		}
		if (this.getCoherence() == null) {
			if (logger.isInfoEnabled()) {
				logger.info("No Coherence instance was provided...creating a default instance.");
			}
			this.coherence = this.createCoherence(detectedSessionTypes);
		}
		if (this.getCoherenceServer() == null) {
			if (logger.isInfoEnabled()) {
				logger.info("No Coherence server defined...creating default server.");
			}
			this.coherenceServer = this.createCoherenceServer();
		}
		this.initialized = true;
	}

	/**
	 * Create the {@link CoherenceConfiguration}.
	 * @return the Configuration for a Coherence instance.
	 */
	protected CoherenceConfiguration createCoherenceConfiguration() {
		final CoherenceConfiguration.Builder builder = CoherenceConfiguration.builder();

		if (!CollectionUtils.isEmpty(this.sessionConfigurations)) {
			builder.withSessions(this.sessionConfigurations);
		}

		if (!CollectionUtils.isEmpty(this.lifecycleListeners)) {
			builder.withEventInterceptors(this.lifecycleListeners);
		}
		builder.named("default"); //TODO

		this.coherenceEventListenerCandidates.processEventListeners();
		builder.withEventInterceptors(this.coherenceEventListenerCandidates.getInterceptors());
		return builder.build();
	}

	/**
	 * Creates a {@link Coherence} instance with the {@link CoherenceConfiguration} provided
	 * by {@link #getCoherenceConfiguration()}. The created Coherence instance may either be
	 * a client Coherence instance ({@link Coherence#client(CoherenceConfiguration)}) or a
	 * cluster member instance ({@link Coherence#clusterMember(CoherenceConfiguration)}.
	 * <p>
	 * The rules for determining the instance type are as follows in descending priority:
	 *
	 * <ul>
	 *    <li>Explicit configuration via {@link #getCoherenceInstanceType()}.
	 *    <li>Via the {@link Set} of detected {@link SessionType}s. As soon as {@link SessionType#SERVER} is provided,
	 *        the Coherence instance is configured using {@link Coherence#clusterMember(CoherenceConfiguration)}.
	 *    <li>If the {@link Set} of detected {@link SessionType} is empty,
	 *        the Coherence instances is configured using {@link Coherence#clusterMember(CoherenceConfiguration)}.
	 * </ul>
	 * @param detectedSessionTypes must not be null
	 * @return the Coherence instance
	 */
	protected Coherence createCoherence(Set<SessionType> detectedSessionTypes) {
		Assert.notNull(detectedSessionTypes, "detectedSessionTypes must not be null.");

		if (this.getCoherenceInstanceType() != null) {
			if (CoherenceInstanceType.CLIENT.equals(this.getCoherenceInstanceType())) {
				return Coherence.client(this.getCoherenceConfiguration());
			}
			else if (CoherenceInstanceType.CLUSTER.equals(this.getCoherenceInstanceType())) {
				return Coherence.clusterMember(this.getCoherenceConfiguration());
			}
			else {
				throw new IllegalStateException("Unsupported CoherenceInstanceType: " + this.getCoherenceInstanceType());
			}
		}
		else {
			if (detectedSessionTypes.isEmpty() || detectedSessionTypes.contains(SessionType.SERVER)) {
				return Coherence.clusterMember(this.getCoherenceConfiguration());
			}
			else {
				return Coherence.client(this.getCoherenceConfiguration());
			}
		}
	}

	/**
	 * Create a {@link CoherenceServer} using the provided {@link Coherence} instance. If
	 * {@link #getCoherenceServerStartupTimeout()} is not null, also provide it to the {@link CoherenceServer}. If
	 * {@link #getCoherenceServerStartupTimeout()} is null, the {@link CoherenceServer} will use the timout specified by
	 * {@link CoherenceServer#DEFAULT_STARTUP_TIMEOUT_MILLIS}.
	 * @return the created CoherenceServer, never null
	 */
	protected CoherenceServer createCoherenceServer() {
		if (this.getCoherenceServerStartupTimeout() != null) {
			return new CoherenceServer(this.getCoherence(), this.getCoherenceServerStartupTimeout());
		}
		else {
			return new CoherenceServer(this.getCoherence());
		}
	}

	/**
	 * Return the Coherence Server startup timeout value.
	 * @return may be null
	 */
	public Duration getCoherenceServerStartupTimeout() {
		return this.coherenceServerStartupTimeout;
	}

	/**
	 * Set the Coherence Server startup timeout value. This is an optional property. If not specified
	 * the {@link CoherenceServer} will use {@link CoherenceServer#DEFAULT_STARTUP_TIMEOUT_MILLIS}.
	 * @param coherenceServerStartupTimeout must be a positive value
	 * @see CoherenceServer
	 */
	public void setCoherenceServerStartupTimeout(Duration coherenceServerStartupTimeout) {
		Assert.isTrue(!coherenceServerStartupTimeout.isNegative(), "coherenceServerStartupTimeout must be positive");
		this.coherenceServerStartupTimeout = coherenceServerStartupTimeout;
	}

	/**
	 * Set the Coherence instance type explicitly. This is an optional property. If not specified, the
	 * {@link CoherenceInstanceType} will be defined depending on the configured Coherence
	 * {@link com.tangosol.net.Session}s. See {@link #createCoherence(Set)} for further details.
	 * @param coherenceInstanceType explicitly set the CoherenceInstanceType
	 */
	public void setCoherenceInstanceType(CoherenceInstanceType coherenceInstanceType) {
		this.coherenceInstanceType = coherenceInstanceType;
	}

	/**
	 * Return the configured {@link CoherenceInstanceType}.
	 * @return value may be null
	 */
	public CoherenceInstanceType getCoherenceInstanceType() {
		return this.coherenceInstanceType;
	}
}
