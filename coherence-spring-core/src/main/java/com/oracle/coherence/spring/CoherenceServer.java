/*
 * File: CoherenceServer.java
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
package com.oracle.coherence.spring;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;

import com.tangosol.coherence.config.Config;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;

/**
 * Responsible for starting the default
 * {@link com.tangosol.net.Coherence} instance.
 *
 * @author Gunnar Hillert
 * @since 1.0
 */
public class CoherenceServer implements InitializingBean, DisposableBean, Lifecycle, ApplicationContextAware {

	/**
	 * Start up default of 15 seconds.
	 */
	private static final long DEFAULT_STARTUP_TIMEOUT_MILLIS = 15000;

	/**
	 * The bean context.
	 */
	private ApplicationContext applicationContext;

	/**
	 * The {@link com.tangosol.net.Coherence} instance to run.
	 */
	private final Coherence coherence;

	/**
	 * {@link Coherence} startup timeout in milliseconds.
	 */
	private final long startupTimeout = Config.getLong("coherence.spring.server.startup-timeout-millis",
			DEFAULT_STARTUP_TIMEOUT_MILLIS);

	/**
	 * Create a {@link CoherenceServer}.
	 *
	 * @param coherence the {@link com.tangosol.net.Coherence} instance to run
	 */
	public CoherenceServer(Coherence coherence) {
		this.coherence = coherence;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}

	@Override
	public void destroy() throws Exception {
		stop();
	}

	public <K, V> NamedCache<K, V> getCache(String cacheName) {
		return this.coherence.getSession().getCache(cacheName);
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public Coherence getCoherence() {
		return coherence;
	}

	@Override
	public boolean isRunning() {
		return this.coherence != null && this.coherence.isStarted();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void start() {
		try {
			coherence.start().get(startupTimeout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new IllegalStateException(String.format("Oracle Coherence did not start "
					+ "successfully within within the specified timeout period of %sms", startupTimeout), e);
		}
	}

	@Override
	public void stop() {
		if (coherence != null) {
			coherence.close();
		}
		Coherence.closeAll();
	}
}
