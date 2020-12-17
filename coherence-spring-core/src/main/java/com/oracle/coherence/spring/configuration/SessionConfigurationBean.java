/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.tangosol.net.Coherence;
import com.tangosol.net.SessionConfiguration;

/**
 * A {@link SessionConfiguration} bean that will be created for
 * each named session in the application configuration properties.
 * <p>Sessions are configured with the {@code coherence.session} prefix,
 * for example {@code coherence.session.foo} configures a session named
 * foo.</p>
 * <p>The session name {@code default} is a special case that configures
 * the default session named {@link com.tangosol.net.Coherence#DEFAULT_NAME}.</p>
 *
 * @author Gunnar Hillert
 *
 */
public class SessionConfigurationBean {

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
	 *
	 * @param name the name for the session
	 */
	protected SessionConfigurationBean(String name) {
		setName(name);
	}

	public SessionConfigurationBean() {
		super();
	}

	public SessionConfiguration getConfiguration() {
		SessionConfiguration.Builder builder = SessionConfiguration
				.builder()
				.named(processSessionName(name))
				.withPriority(priority);

		if (scopeName != null) {
			builder = builder.withScopeName(scopeName);
		}
		if (configUri != null) {
			builder = builder.withConfigUri(configUri);
		}
		return builder.build();
	}

	/**
	 * Set the name of this configuration.
	 *
	 * @param name the name of this configuration
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the scope name for this configuration.
	 *
	 * @param scopeName the scope name for this configuration
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * Get the Coherence cache configuration URI.
	 *
	 * @return the Coherence cache configuration URI
	 */
	public String getConfig() {
		return configUri;
	}

	/**
	 * Set the Coherence cache configuration URI.
	 *
	 * @param configUri the Coherence cache configuration URI
	 */
	public void setConfig(String configUri) {
		this.configUri = configUri;
	}

	/**
	 * Set the priority for this configuration.
	 * <p>{@link com.tangosol.net.Session Sessions} are started lowest priority first
	 * and closed in reverse order.</p>
	 *
	 * @param priority the priority for this configuration
	 * @see com.tangosol.net.SessionConfiguration#getPriority()
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * A marker annotation on a {@link com.tangosol.net.SessionConfiguration} or
	 * a {@link com.tangosol.net.SessionConfiguration.Provider} to indicate that
	 * it replaces another configuration with the same name.
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Replaces {
	}

	public String getName() {
		return name;
	}

	public String getScopeName() {
		return scopeName;
	}

	public int getPriority() {
		return priority;
	}

	private String processSessionName(String sessionName) {
		return DEFAULT_SESSION_NAME.equalsIgnoreCase(sessionName) ? Coherence.DEFAULT_NAME : sessionName;
	}
}
