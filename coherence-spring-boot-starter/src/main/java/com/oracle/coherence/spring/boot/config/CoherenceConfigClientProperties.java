/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	private String sessionName;

	/**
	 * Name of the Coherence scope used to fetch remote properties from. If not set, the default scope is used.
	 */
	private String scopeName;

	/**
	 * Flag to indicate that failure to connect to the server is fatal (default false).
	 */
	private boolean failFast = false;

	/**
	 * Contains gRPC-specific configuration.
	 */
	private GrpcClientProperties client = new GrpcClientProperties();

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
	 * Contains gRPC-specific configuration.
	 * @return the gRPC-specific configuration properties
	 */
	public GrpcClientProperties getClient() {
		return this.client;
	}

	public void setClient(GrpcClientProperties client) {
		this.client = client;
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

	/**
	 * Coherence gRPC client configuration.
	 */
	public static class GrpcClientProperties {

		private String host = "localhost";
		private int port = 1408;
		private boolean enableTls = false;

		/**
		 * Returns host name of gRPC server.
		 * @return host name
		 */
		public String getHost() {
			return this.host;
		}

		/**
		 * Sets host name of gRPC server.
		 * @param host host name
		 */
		public void setHost(String host) {
			this.host = host;
		}

		/**
		 * Gets gRPC server port.
		 * @return port
		 */
		public int getPort() {
			return this.port;
		}

		/**
		 * Sets gRPC server port.
		 * @param port port
		 */
		public void setPort(int port) {
			this.port = port;
		}

		/**
		 * Returns true if TLS is enabled.
		 * @return true if TLS is enabled
		 */
		public boolean isEnableTls() {
			return this.enableTls;
		}

		/**
		 * Enables TLS support.
		 * @param enableTls if not set, defaults to true. Set to false to disable TLS.
		 */
		public void setEnableTls(boolean enableTls) {
			this.enableTls = enableTls;
		}

		@Override
		public String toString() {
			return "CoherenceClientConfiguration{" +
					"host='" + this.host + '\'' +
					", port=" + this.port +
					", enableTls=" + this.enableTls +
					'}';
		}
	}
}
