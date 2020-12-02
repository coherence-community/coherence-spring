/*
 * File: CoherenceSpringConfiguration.java
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
package com.oracle.coherence.spring.configuration;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.spring.CoherenceServer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.Session;

/**
 *
 * @author Gunnar Hillert
 *
 */
@Configuration
public class CoherenceSpringConfiguration {

	protected static final Log logger = LogFactory.getLog(CoherenceSpringConfiguration.class);

	@Autowired
	private ConfigurableApplicationContext context;

	private Coherence coherence;
	private CoherenceConfiguration coherenceConfiguration;
	private CoherenceServer coherenceServer;

	private boolean initialized = false;

	@Bean
	public Coherence getCoherence() {
		return coherence;
	}

	@Bean
	public CoherenceConfiguration getCoherenceConfiguration() {
		return coherenceConfiguration;
	}

	@Bean
	public CoherenceServer getCoherenceServer() {
		return coherenceServer;
	}

	/**
	 * Return the Coherence {@link Cluster}.
	 *
	 * @return the Coherence {@link Cluster} (which may or may not be running)
	 */
	@Bean
	@DependsOn("coherenceServer")
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public Cluster getCluster() {
		return CacheFactory.getCluster();
	}

	/**
	 * Create a {@link com.tangosol.net.Session} from the qualifiers on the specified
	 * injection point.
	 *
	 * @param injectionPoint the injection point that the {@link com.tangosol.net.Session}
	 *                       will be injected into
	 * @return a {@link com.tangosol.net.Session}
	 */
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public Session getSession(InjectionPoint injectionPoint) {
		final Name nameAnnotation = injectionPoint.getAnnotation(Name.class);
		final String sessionName;

		if (nameAnnotation == null) {
			sessionName = Coherence.DEFAULT_NAME;
		}
		else {
			sessionName = nameAnnotation.value();
		}

		return Coherence.findSession(sessionName)
				.orElseThrow(() -> new IllegalStateException("No Session has been configured with the name " + sessionName));
	}

	/**
	 * Sets up the basic components used by Coherence. These are extracted from the
	 * underlying {@link CoherenceConfigurer}, defaulting to sensible values.
	 *
	 * @throws Exception if there is a problem in the configurer
	 */
	@PostConstruct
	protected void initialize() throws Exception {
		if (initialized) {
			return;
		}

		final CoherenceConfigurer coherenceConfigurer = getConfigurer();

		logger.debug(String.format("Using %s CoherenceConfigurer", coherenceConfigurer.getClass().getName()));

		this.coherence = coherenceConfigurer.getCoherence();
		this.coherenceConfiguration = coherenceConfigurer.getCoherenceConfiguration();
		this.coherenceServer = coherenceConfigurer.getCoherenceServer();

		this.initialized = true;

	}

	private CoherenceConfigurer getConfigurer() throws Exception {
		int numberOfConfigurers = this.context.getBeanNamesForType(CoherenceConfigurer.class).length;

		if (numberOfConfigurers < 1) {
			final DefaultCoherenceConfigurer coherenceConfigurer = new DefaultCoherenceConfigurer();
			coherenceConfigurer.initialize();
			this.context.getBeanFactory().registerSingleton("coherenceConfigurer",
					coherenceConfigurer);
			return coherenceConfigurer;
		}
		else {
			if (numberOfConfigurers == 1) {
				return this.context.getBean(CoherenceConfigurer.class);
			}
			else {
				throw new IllegalStateException(
						"Expected one CoherenceConfigurer but found " + numberOfConfigurers);
			}
		}
	}
}
