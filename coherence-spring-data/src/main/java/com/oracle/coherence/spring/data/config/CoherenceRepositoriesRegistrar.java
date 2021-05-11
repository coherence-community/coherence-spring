/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.config;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.data.support.CoherenceRepositoryConfigurationExtension;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * This class wil register support for Coherence repositories with the Spring runtime.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherenceRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableCoherenceRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new CoherenceRepositoryConfigurationExtension();
	}
}
