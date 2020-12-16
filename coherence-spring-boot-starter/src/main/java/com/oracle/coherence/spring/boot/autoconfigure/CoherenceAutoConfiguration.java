/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.cache.CoherenceCacheManager;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.net.Coherence;

/**
 * Activates Coherence Auto Configuration for Spring Boot, provided the respective
 * {@link CoherenceServer} is not defined.
 *
 * @author Gunnar Hillert
 *
 */
@Configuration
@ConditionalOnMissingBean(CoherenceServer.class)
@EnableCoherence
@EnableConfigurationProperties(CoherenceProperties.class)
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class CoherenceAutoConfiguration {

	@Autowired
	private CoherenceProperties coherenceProperties;

	@Bean
	@Conditional(CachingEnabledCondition.class)
	@ConditionalOnMissingBean(CacheManager.class)
	CoherenceCacheManager cacheManager(Coherence coherence) {
		return new CoherenceCacheManager(coherence);
	}

}
