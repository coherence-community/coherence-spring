/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.oracle.coherence.spring.cache.CoherenceCacheManager;
import com.oracle.coherence.spring.configuration.support.CoherenceAnnotationUtils;
import com.oracle.coherence.spring.configuration.support.SpringSystemPropertyResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * A {@link ImportBeanDefinitionRegistrar} that will determine whether caching is enabled. If caching is enabled
 * this registrar will also check if a {@link CacheManager} already exists and if not will provide a default
 * {@link CoherenceCacheManager}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 * @see CoherenceSpringConfiguration
 */
public class EnableCoherenceImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

	protected static final Log logger = LogFactory.getLog(CoherenceSpringConfiguration.class);

	final Environment environment;
	final BeanFactory beanFactory;

	public EnableCoherenceImportBeanDefinitionRegistrar(Environment environment, BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.environment = environment;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
		final String[] beanNames = registry.getBeanDefinitionNames();

		boolean cachingEnabled = false;
		boolean cacheManagerFound = false;
		boolean springSystemPropertyResolverFound = false;

		for (String beanName : beanNames) {
			final Class<?> beanType = CoherenceAnnotationUtils.getBeanTypeForBeanName(this.beanFactory, beanName);
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

		if (this.logger.isInfoEnabled()) {
			this.logger.info(String.format("Caching is enabled: %s. Found existing CacheManager: %s", cachingEnabled, cacheManagerFound));
		}

		if (!springSystemPropertyResolverFound) {
			registry.registerBeanDefinition(CoherenceSpringConfiguration.SPRING_SYSTEM_PROPERTY_RESOLVER_BEAN_NAME,
					BeanDefinitionBuilder.genericBeanDefinition(SpringSystemPropertyResolver.class)
							.addConstructorArgValue(this.environment).getBeanDefinition());
		}

		if (cachingEnabled && !cacheManagerFound) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Creating default CacheManager.");
			}

			registry.registerBeanDefinition("cacheManager", BeanDefinitionBuilder.genericBeanDefinition(CoherenceCacheManager.class)
					.addConstructorArgReference(CoherenceSpringConfiguration.COHERENCE_BEAN_NAME).getBeanDefinition());
		}
	}
}
