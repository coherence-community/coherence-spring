/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.support;

import java.util.Collection;
import java.util.Collections;

import com.oracle.coherence.spring.configuration.CoherenceSpringConfiguration;
import com.oracle.coherence.spring.data.repository.CoherenceRepository;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

/**
 * Coherence implementation of {@link RepositoryConfigurationExtensionSupport}.
 *
 * @author Ryan Lubke
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {
	@Override
	public String getRepositoryFactoryBeanClassName() {
		return CoherenceRepositoryFactoryBean.class.getName();
	}

	@Override
	public String getModuleName() {
		return "Coherence";
	}

	@Override
	protected String getModulePrefix() {
		return "coherence";
	}

	@Override
	protected Collection<Class<?>> getIdentifyingTypes() {
		return Collections.singleton(CoherenceRepository.class);
	}

	@Override
	public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
		builder.addDependsOn(CoherenceSpringConfiguration.COHERENCE_SERVER_BEAN_NAME);
		builder.setLazyInit(true);
		builder.addPropertyReference("coherence", CoherenceSpringConfiguration.COHERENCE_BEAN_NAME);
	}
}
