/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure;

import java.util.List;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.config.CoherenceConfigClientProperties;
import com.oracle.coherence.spring.cache.CoherenceCacheManager;
import com.oracle.coherence.spring.configuration.CoherenceSpringConfiguration;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.AbstractSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.support.SpringSystemPropertyResolver;
import com.tangosol.net.Coherence;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.CollectionUtils;

/**
 * Activates Coherence Auto Configuration for Spring Boot, provided the respective
 * {@link CoherenceServer} is not defined.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
@ConditionalOnMissingBean(CoherenceServer.class)
@EnableCoherence
@EnableConfigurationProperties({
		CoherenceProperties.class,
		CoherenceConfigClientProperties.class})
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class CoherenceAutoConfiguration {

	@Bean
	@Conditional(CachingEnabledCondition.class)
	@ConditionalOnMissingBean(CacheManager.class)
	CoherenceCacheManager cacheManager(Coherence coherence) {
		return new CoherenceCacheManager(coherence);
	}

	@Bean
	public static BeanFactoryPostProcessor coherenceAutoConfigurationBeanFactoryPostProcessor(ConfigurableEnvironment environment) {
		return (beanFactory) -> {
			BindResult<CoherenceProperties> result = Binder.get(environment)
					.bind("coherence", CoherenceProperties.class);

			if (!result.isBound()) {
				return;
			}

			final CoherenceProperties coherenceProperties = result.get();

			environment.getPropertySources().addFirst(
					new MapPropertySource("coherence.properties", coherenceProperties.retrieveCoherencePropertiesAsMap()));

			final BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

			if (registry.containsBeanDefinition(CoherenceSpringConfiguration.SPRING_SYSTEM_PROPERTY_RESOLVER_BEAN_NAME)) {
				registry.removeBeanDefinition("springSystemPropertyResolver");
			}

			registry.registerBeanDefinition(CoherenceSpringConfiguration.SPRING_SYSTEM_PROPERTY_RESOLVER_BEAN_NAME,
					BeanDefinitionBuilder.genericBeanDefinition(SpringSystemPropertyResolver.class)
							.addConstructorArgValue(environment)
							.addConstructorArgValue(coherenceProperties.getPropertyPrefix())
							.getBeanDefinition());

			final List<AbstractSessionConfigurationBean> sessionConfigurationBeans = coherenceProperties.getSessions().getAllSessionConfigurationBeans();
			if (!CollectionUtils.isEmpty(sessionConfigurationBeans)) {
				int count = 1;
				for (AbstractSessionConfigurationBean sessionConfigurationBean : sessionConfigurationBeans) {
					beanFactory.registerSingleton("sessionConfigurationBean_" + count, sessionConfigurationBean);
					count++;
				}
			}
		};
	}

}
