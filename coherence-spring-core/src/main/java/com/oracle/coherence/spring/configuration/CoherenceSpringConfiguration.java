/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import javax.annotation.PostConstruct;

import com.oracle.coherence.common.base.Classes;
import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.configuration.support.CoherenceConfigurerCustomizer;
import com.oracle.coherence.spring.event.CoherenceEventListenerCandidates;
import com.oracle.coherence.spring.event.CoherenceEventListenerMethodProcessor;
import com.oracle.coherence.spring.event.mapevent.MapListenerRegistrationBean;
import com.oracle.coherence.spring.messaging.CoherenceTopicListenerPostProcessor;
import com.oracle.coherence.spring.messaging.CoherenceTopicListenerSubscribers;
import com.tangosol.io.Serializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Cluster;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.OperationalContext;
import com.tangosol.net.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

/**
 * Main configuration class to configure Coherence.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
@Import({
		CoherenceConversionServicePostProcessor.class,
		NamedCacheConfiguration.class,
		ExtractorConfiguration.class,
		FilterConfiguration.class,
		ExtractorService.class,
		FilterService.class,
		MapEventTransformerService.class,
		MapEventTransformerConfiguration.class,
		NamedTopicConfiguration.class,
		CoherenceTopicListenerSubscribers.class
})
@PropertySource("classpath:coherence-spring.properties")
public class CoherenceSpringConfiguration {

	protected static final Log logger = LogFactory.getLog(CoherenceSpringConfiguration.class);

	private final ConfigurableApplicationContext context;

	private Coherence coherence;
	private CoherenceConfiguration coherenceConfiguration;
	private CoherenceServer coherenceServer;

	private boolean initialized = false;

	/**
	 * The name of the default {@link Coherence} bean.
	 */
	public static final String COHERENCE_BEAN_NAME = "coherence";

	/**
	 * The name of the default {@link CoherenceConfiguration} bean.
	 */
	public static final String COHERENCE_CONFIGURATION_BEAN_NAME = "coherenceConfiguration";

	/**
	 * The name of the default {@link CoherenceServer} bean.
	 */
	public static final String COHERENCE_SERVER_BEAN_NAME = "coherenceServer";

	/**
	 * The name of the default Coherence {@link Cluster} bean.
	 */
	public static final String COHERENCE_CLUSTER_BEAN_NAME = "coherenceCluster";

	/**
	 * The name of the default Coherence {@link Cluster} bean.
	 */
	public static final String SPRING_SYSTEM_PROPERTY_RESOLVER_BEAN_NAME = "springSystemPropertyResolver";

	/**
	 * Candidates for Coherence event listeners.
	 */
	private CoherenceEventListenerCandidates coherenceEventListenerCandidates;

	public CoherenceSpringConfiguration(ConfigurableApplicationContext context) {
		this.context = context;
	}

	/**
	 * The name of the {@link CoherenceConfigurer} bean.
	 */
	public static final String COHERENCE_CONFIGURER_BEAN_NAME = "coherenceConfigurer";

	@Bean(COHERENCE_BEAN_NAME)
	@DependsOn(SPRING_SYSTEM_PROPERTY_RESOLVER_BEAN_NAME)
	public Coherence getCoherence() {
		return this.coherence;
	}

	@Bean(COHERENCE_CONFIGURATION_BEAN_NAME)
	public CoherenceConfiguration getCoherenceConfiguration() {
		return this.coherenceConfiguration;
	}

	@Bean(COHERENCE_SERVER_BEAN_NAME)
	@DependsOn(SPRING_SYSTEM_PROPERTY_RESOLVER_BEAN_NAME)
	public CoherenceServer getCoherenceServer() {
		return this.coherenceServer;
	}

	/**
	 * Return the Coherence {@link Cluster}.
	 * @return the Coherence {@link Cluster} (which may or may not be running)
	 */
	@Bean(COHERENCE_CLUSTER_BEAN_NAME)
	@DependsOn(COHERENCE_SERVER_BEAN_NAME)
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public Cluster getCoherenceCluster() {
		return CacheFactory.getCluster();
	}

	/**
	 * Create a {@link com.tangosol.net.Session} from the qualifiers on the specified
	 * injection point. If no {@code Name} annotation is provided, then the default
	 * session is returned.
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

		return Coherence.findSession(sessionName)
				.orElseThrow(() -> new IllegalStateException("No Session has been configured with the name " + sessionName));
	}

	@Bean
	static MapListenerRegistrationBean mapListenerRegistrationBean(
			FilterService filterService,
			MapEventTransformerService mapEventTransformerService) {
		return new MapListenerRegistrationBean(filterService, mapEventTransformerService);
	}

	/**
	 * Sets up the basic components used by Coherence. These are extracted from the
	 * underlying {@link CoherenceConfigurer}, defaulting to sensible values.
	 */
	@PostConstruct
	protected void initialize() {

		if (this.initialized) {
			return;
		}

		final CoherenceConfigurer coherenceConfigurer = getConfigurer();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Using %s CoherenceConfigurer", coherenceConfigurer.getClass().getName()));
		}

		this.coherence = coherenceConfigurer.getCoherence();
		this.coherenceConfiguration = coherenceConfigurer.getCoherenceConfiguration();
		this.coherenceServer = coherenceConfigurer.getCoherenceServer();

		this.initialized = true;
	}

	private CoherenceConfigurer getConfigurer() {
		int numberOfConfigurers = this.context.getBeanNamesForType(CoherenceConfigurer.class).length;

		if (numberOfConfigurers < 1) {
			final DefaultCoherenceConfigurer coherenceConfigurer = new DefaultCoherenceConfigurer(this.context, this.coherenceEventListenerCandidates);

			try {
				final CoherenceConfigurerCustomizer<DefaultCoherenceConfigurer> customizer = this.context.getBean(CoherenceConfigurerCustomizer.class);
				customizer.customize(coherenceConfigurer);
			}
			catch (NoSuchBeanDefinitionException ex) {
				//Ignore
			}

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
	public static CoherenceEventListenerMethodProcessor coherenceEventListenerMethodProcessor() {
		return new CoherenceEventListenerMethodProcessor();
	}

	@Autowired
	public void setCoherenceEventListenerCandidates(CoherenceEventListenerCandidates coherenceEventListenerCandidates) {
		this.coherenceEventListenerCandidates = coherenceEventListenerCandidates;
	}

	@Bean
	public static CoherenceTopicListenerPostProcessor coherenceTopicListenerPostProcessor() {
		return new CoherenceTopicListenerPostProcessor();
	}

	/**
	 * A factory method to produce the default Java {@link Serializer}.
	 * @return the default Java {@link Serializer}
	 */
	@Qualifier("java")
	@Bean
	public Serializer defaultSerializer() {
		final OperationalContext operationalContext = ((OperationalContext) this.getCoherenceCluster());
		return operationalContext.getSerializerMap().get("java")
				.createSerializer(Classes.getContextClassLoader());
	}

	/**
	 * A factory method to produce the default
	 * Java {@link Serializer}.
	 * @return the default Java {@link Serializer}
	 */
	@Qualifier("pof")
	@Bean
	public Serializer pofSerializer() {
		final OperationalContext operationalContext = ((OperationalContext) this.getCoherenceCluster());
		return operationalContext.getSerializerMap().get("pof")
				.createSerializer(Classes.getContextClassLoader());
	}
}
