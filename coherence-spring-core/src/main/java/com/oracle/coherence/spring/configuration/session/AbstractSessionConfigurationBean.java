/*
 * Copyright 2017-2020 original authors
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
package com.oracle.coherence.spring.configuration.session;

import com.tangosol.net.SessionConfiguration;

import org.springframework.util.Assert;

/**
 * A base {@link SessionConfigurationProvider}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 *
 * @see SessionConfiguration
 */
public abstract class AbstractSessionConfigurationBean implements SessionConfigurationProvider {

	/**
	 * Name of the default session.
	 */
	public static final String DEFAULT_SESSION_NAME = "default";

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
	 * The priority order to use when starting the {@link com.tangosol.net.Session}.
	 * <p>
	 * Sessions will be started lowest priority first.
	 * @see SessionConfiguration#DEFAULT_PRIORITY
	 */
	private int priority = SessionConfiguration.DEFAULT_PRIORITY;

	/**
	 * Create a {@link AbstractSessionConfigurationBean}. It will use te default session by default.
	 */
	protected AbstractSessionConfigurationBean() {
	}

	/**
	 * Create a named {@link AbstractSessionConfigurationBean}.
	 * @param name must not be empty
	 */
	protected AbstractSessionConfigurationBean(String name) {
		Assert.hasText(name, "name must not be empty.");
		this.name = name;
	}

	/**
	 * Set the name of this configuration.
	 * @param name the name of this configuration
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return the name of this configuration.
	 * @return the name of this configuration
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the scope name for this configuration.
	 * @param scopeName the scope name for this configuration
	 */
	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	/**
	 * Return the scope name for this configuration.
	 * @return the scope name for this configuration
	 */
	public String getScopeName() {
		return this.scopeName;
	}

	/**
	 * Set the priority for this configuration.
	 * <p>
	 * {@link com.tangosol.net.Session Sessions} are started lowest priority first
	 * and closed in reverse order.
	 * @param priority the priority for this configuration
	 * @see SessionConfiguration#getPriority()
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Returns the priority of this configuration.
	 * @return the priority of this configuration
	 */
	public int getPriority() {
		return this.priority;
	}

	/**
	 * Set the priority of this configuration.
	 * @param sessionType   the type of this {@link com.tangosol.net.Session}
	 */
	public void setType(SessionType sessionType) {
		Assert.notNull(sessionType, "SessionType must not be null.");
		this.type = sessionType;
	}

	/**
	 * Returns the type of the session.
	 * @return the type of this configuration
	 */
	public SessionType getType() {
		return this.type;
	}
}
