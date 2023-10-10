/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.session;

import java.util.Optional;

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
 * @since 3.0
 */
public class SessionConfigurationBean extends AbstractSessionConfigurationBean {

	/**
	 * The Coherence cache configuration URI for the session.
	 */
	private String configUri;

	/**
	 * Create a named {@link SessionConfigurationBean}.
	 * @param name the name for the session
	 */
	protected SessionConfigurationBean(String name) {
		super(name);
	}

	public SessionConfigurationBean() {
		super();
	}

	@Override
	public Optional<SessionConfiguration> getConfiguration() {
		SessionConfiguration.Builder builder = SessionConfiguration
				.builder()
				.named(processSessionName(this.getName()))
				.withPriority(this.getPriority());

		if (this.getScopeName() != null) {
			builder = builder.withScopeName(this.getScopeName());
		}
		if (this.configUri != null) {
			builder = builder.withConfigUri(this.configUri);
		}
		return Optional.of(builder.build());
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

	private String processSessionName(String sessionName) {
		return DEFAULT_SESSION_NAME.equalsIgnoreCase(sessionName) ? Coherence.DEFAULT_NAME : sessionName;
	}
}
