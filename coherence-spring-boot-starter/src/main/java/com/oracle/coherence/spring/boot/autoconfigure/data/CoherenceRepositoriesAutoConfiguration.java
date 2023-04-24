/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure.data;

import com.oracle.coherence.spring.data.repository.CoherenceRepository;
import com.oracle.coherence.spring.data.support.CoherenceRepositoryConfigurationExtension;
import com.oracle.coherence.spring.data.support.CoherenceRepositoryFactoryBean;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} for Coherence Repositories
 * as part of the Spring Data support. Auto-configuration equivalent of enabling Coherence repositories
 * using the {@link com.oracle.coherence.spring.data.config.EnableCoherenceRepositories} annotation.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(CoherenceRepository.class)
@ConditionalOnMissingBean({ CoherenceRepositoryFactoryBean.class, CoherenceRepositoryConfigurationExtension.class })
@ConditionalOnProperty(prefix = "coherence.spring.data.repositories", name = "enabled", havingValue = "true",
		matchIfMissing = true)
@Import(CoherenceRepositoriesRegistrar.class)
public class CoherenceRepositoriesAutoConfiguration {
}
