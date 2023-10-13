/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Contains Spring Boot @{@link ConfigurationProperties} for retrieval of configuration properties stored in a remote
 * Coherence cluster.
 * @author Gunnar Hillert
 * @since 3.0
 */
@ConfigurationProperties(CoherenceConfigClientProperties.PREFIX)
@Validated
public class CoherenceConfigClientProperties {

	/**
	 * Prefix for configuration properties.
	 */
	public static final String PREFIX = "coherence.config-client";

	/**
	 * Default profile value.
	 */
	public static final String DEFAULT_PROFILE = "default";

	/**
	 * Are the facilities to retrieve remote Coherence configuration properties enabled? Defaults to {@code true}.
	 */
	private boolean enabled = true;

	/**
	 * The default profile to use when fetching remote configuration (comma-separated).
	 * Default is "default".
	 */
	private String profile = DEFAULT_PROFILE;

	/**
	 * Name of the application used to fetch remote properties.
	 */
	private String applicationName;

	/**
	 * Name of the Coherence session used to fetch remote properties from. If not set, the default session is used.
	 */
	private String sessionName = "";

	/**
	 * Name of the Coherence scope used to fetch remote properties from. If not set, the default scope is used.
	 */
	private String scopeName;

	/**
	 * Flag to indicate that failure to connect to the server is fatal (default false).
	 */
	private boolean failFast = false;

	/**
	 * Contains client-specific Coherence configuration. This can be used to configure Coherence*Extend and
	 * gRPC clients.
	 */
	private String cacheConfig = "coherence-cache-config.xml";

	/**
	 * The default profile to use when fetching remote configuration (comma-separated).
	 * Default is "default".
	 * @return the specified Spring profile
	 */
	public String getProfile() {
		return this.profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	/**
	 * Flag to indicate that failure to connect to the server is fatal (default false).
	 * @return true if FailFast is enabled.
	 */
	public boolean isFailFast() {
		return this.failFast;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	/**
	 * Returns {@code true} if the facilities to retrieve remote Coherence configuration properties are enabled?
	 * Defaults to {@code true} if not set.
	 * @return true if the retrieval of remote Coherence configuration properties is enabled
	 */
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Name of the application used to fetch remote properties.
	 * @return the name of the application to retrieve remote properties for
	 */
	public String getApplicationName() {
		return this.applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	/**
	 * Name of the Coherence session used to fetch remote properties from. If not set, the default session is used.
	 * @return the name of the specified Coherence session
	 */
	public String getSessionName() {
		return this.sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	/**
	 * Name of the Coherence scope used to fetch remote properties from. If not set, the default scope is used.
	 * @return the scope name if set
	 */
	public String getScopeName() {
		return this.scopeName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	public String getCacheConfig() {
		return this.cacheConfig;
	}

	public void setCacheConfig(String cacheConfig) {
		this.cacheConfig = cacheConfig;
	}

}
