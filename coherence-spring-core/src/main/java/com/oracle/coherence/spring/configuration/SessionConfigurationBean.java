/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.Optional;

import com.oracle.coherence.spring.configuration.support.SessionType;
import com.tangosol.net.Coherence;
import com.tangosol.net.SessionConfiguration;

/**
 * A {@link SessionConfiguration} bean that will be created for
 * each named session in the application configuration properties.
 * <p>
 * Sessions are configured with the {@code coherence.session} prefix,
 * for example {@code coherence.session.foo} configures a session named
 * foo.
 * <p>
 * The session name {@code default} is a special case that configures
 * the default session named {@link com.tangosol.net.Coherence#DEFAULT_NAME}.
 *
 * @author Gunnar Hillert
 *
 */
public class SessionConfigurationBean implements SessionConfigurationProvider {

	private static final String DEFAULT_SESSION_NAME = "default";

	/**
	 * The name of the session.
	 */
	private String name;

	/**
	 * The scope name for the session.
	 */
	private String scopeName;

	/**
	 * The type of this configuration.
	 */
	private SessionType type;

	/**
	 * The Coherence cache configuration URI for the session.
	 */
	private String configUri;

	/**
	 * The priority order to use when starting the {@link com.tangosol.net.Session}.
	 * <p>
	 * Sessions will be started lowest priority first.
	 * @see com.tangosol.net.SessionConfiguration#DEFAULT_PRIORITY
	 */
	private int priority = SessionConfiguration.DEFAULT_PRIORITY;

	/**
	 * Create a named {@link SessionConfigurationBean}.
	 * @param name the name for the session
	 */
	protected SessionConfigurationBean(String name) {
		setName(name);
	}

	public SessionConfigurationBean() {
		super();
	}

	@Override
	public Optional<SessionConfiguration> getConfiguration() {
		final SessionType type = getType();
		if (SessionType.GRPC == type) {
			return Optional.empty();
		}

		SessionConfiguration.Builder builder = SessionConfiguration
				.builder()
				.named(processSessionName(this.getName()))
				.withPriority(this.getPriority());

		if (this.scopeName != null) {
			builder = builder.withScopeName(this.scopeName);
		}
		if (this.configUri != null) {
			builder = builder.withConfigUri(this.configUri);
		}
		return Optional.of(builder.build());
	}

	/**
	 * Set the name of this configuration.
	 * @param name the name of this configuration
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the scope name for this configuration.
	 * @param scopeName the scope name for this configuration
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * Get the Coherence cache configuration URI.
	 * @return the Coherence cache configuration URI
	 */
	public String getConfig() {
		return this.configUri;
	}

	/**
	 * Set the Coherence cache configuration URI.
	 * @param configUri the Coherence cache configuration URI
	 */
	public void setConfig(String configUri) {
		this.configUri = configUri;
	}

	/**
	 * Set the priority for this configuration.
	 * <p>
	 * {@link com.tangosol.net.Session Sessions} are started lowest priority first
	 * and closed in reverse order.
	 * @param priority the priority for this configuration
	 * @see com.tangosol.net.SessionConfiguration#getPriority()
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Set the priority of this configuration.
	 * @param type  the type of this configuration
	 */
	public void setType(SessionType type) {
		this.type = type;
	}

	public String getName() {
		return this.name;
	}

	public String getScopeName() {
		return this.scopeName;
	}

	public int getPriority() {
		return this.priority;
	}

	public SessionType getType() {
		return this.type;
	}

	public String getConfigUri() {
		return this.configUri;
	}

	private String processSessionName(String sessionName) {
		return DEFAULT_SESSION_NAME.equalsIgnoreCase(sessionName) ? Coherence.DEFAULT_NAME : sessionName;
	}
}
