/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import javax.annotation.PostConstruct;

import com.oracle.coherence.spring.configuration.support.SpringSystemPropertyResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationUtils;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.cache.CoherenceCacheManager;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.Session;
import org.springframework.core.env.Environment;

/**
 * Main configuration class to configure Coherence.
 *
 * @author Gunnar Hillert
 *
 */
@Configuration
@Import(NamedCacheConfiguration.class)
@PropertySource("classpath:coherence-spring.properties")
public class CoherenceSpringConfiguration {

	protected static final Log logger = LogFactory.getLog(CoherenceSpringConfiguration.class);

	@Autowired
	private ConfigurableApplicationContext context;

	private Coherence coherence;
	private CoherenceConfiguration coherenceConfiguration;
	private CoherenceServer coherenceServer;

	private boolean initialized = false;

	public static final String COHERENCE_BEAN_NAME = "coherence";
	public static final String COHERENCE_CONFIGURATION_BEAN_NAME = "coherenceConfiguration";
	public static final String COHERENCE_SERVER_BEAN_NAME = "coherenceServer";
	public static final String COHERENCE_CLUSTER_BEAN_NAME = "coherenceCluster";
	public static final String COHERENCE_CONFIGURER_BEAN_NAME = "coherenceConfigurer";

	@Bean(name = COHERENCE_BEAN_NAME)
	public Coherence getCoherence() {
		return coherence;
	}

	@Bean(name = COHERENCE_CONFIGURATION_BEAN_NAME)
	public CoherenceConfiguration getCoherenceConfiguration() {
		return coherenceConfiguration;
	}

	@Bean(name = COHERENCE_SERVER_BEAN_NAME)
	public CoherenceServer getCoherenceServer() {
		return coherenceServer;
	}

	/**
	 * Return the Coherence {@link Cluster}.
	 *
	 * @return the Coherence {@link Cluster} (which may or may not be running)
	 */
	@Bean(name = COHERENCE_CLUSTER_BEAN_NAME)
	@DependsOn(COHERENCE_SERVER_BEAN_NAME)
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public Cluster getCoherenceCluster() {
		return CacheFactory.getCluster();
	}

	/**
	 * Create a {@link com.tangosol.net.Session} from the qualifiers on the specified
	 * injection point. If no {@code Name} annotation is provided, then the default
	 * session is returned.
	 *
	 * @param injectionPoint the injection point that the {@link com.tangosol.net.Session}
	 *                       will be injected into
	 * @return a {@link com.tangosol.net.Session}
	 */
	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public Session session(InjectionPoint injectionPoint) {
		final Name nameAnnotation = injectionPoint.getAnnotation(Name.class);
		final String sessionName;

		if (nameAnnotation == null) {
			sessionName = Coherence.DEFAULT_NAME;
		}
		else {
			sessionName = nameAnnotation.value();
		}

		final Session session = Coherence.findSession(sessionName)
				.orElseThrow(() -> new IllegalStateException("No Session has been configured with the name " + sessionName));
		return session;
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
		SpringSystemPropertyResolver s = this.context.getBean(SpringSystemPropertyResolver.class);
		String o = s.getProperty("coherence.log");

		final CoherenceConfigurer coherenceConfigurer = getConfigurer();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Using %s CoherenceConfigurer", coherenceConfigurer.getClass().getName()));
		}

		this.coherence = coherenceConfigurer.getCoherence();
		this.coherenceConfiguration = coherenceConfigurer.getCoherenceConfiguration();
		this.coherenceServer = coherenceConfigurer.getCoherenceServer();

		this.initialized = true;
	}

	private CoherenceConfigurer getConfigurer() throws Exception {
		int numberOfConfigurers = this.context.getBeanNamesForType(CoherenceConfigurer.class).length;

		if (numberOfConfigurers < 1) {
			final DefaultCoherenceConfigurer coherenceConfigurer = new DefaultCoherenceConfigurer(this.context);
			coherenceConfigurer.initialize();
			this.context.getBeanFactory().registerSingleton(COHERENCE_CONFIGURER_BEAN_NAME,
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

	@Bean
	public static BeanFactoryPostProcessor beanFactoryPostProcessor(Environment environment) {
		return beanFactory -> {
			final String[] beanNames = beanFactory.getBeanDefinitionNames();

			boolean cachingEnabled = false;
			boolean cacheManagerFound = false;
			boolean springSystemPropertyResolverFound = false;

			for (String beanName : beanNames) {
				final Class<?> beanType = beanFactory.getType(beanName);
				final EnableCaching enableCaching = AnnotationUtils.findAnnotation(beanType, EnableCaching.class);
				if (enableCaching != null) {
					cachingEnabled = true;
				}
				if (CacheManager.class.isAssignableFrom(beanType)) {
					cacheManagerFound = true;
				}
				if (SpringSystemPropertyResolver.class.isAssignableFrom(beanType)) {
					springSystemPropertyResolverFound = true;
				}
			}

			if (logger.isInfoEnabled()) {
				logger.info(String.format("Caching is enabled: %s. Found existing CacheManager: %s", cachingEnabled, cacheManagerFound));
			}

			final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			if (!springSystemPropertyResolverFound) {
				registry.registerBeanDefinition("springSystemPropertyResolver",
						BeanDefinitionBuilder.genericBeanDefinition(SpringSystemPropertyResolver.class)
								.addConstructorArgValue(environment).getBeanDefinition());
			}

			if (cachingEnabled && !cacheManagerFound) {
				if (logger.isInfoEnabled()) {
					logger.info("Creating default CacheManager.");
				}

				registry.registerBeanDefinition("cacheManager", BeanDefinitionBuilder.genericBeanDefinition(CoherenceCacheManager.class)
						.addConstructorArgReference(COHERENCE_BEAN_NAME).getBeanDefinition());
			}
		};
	}
}
