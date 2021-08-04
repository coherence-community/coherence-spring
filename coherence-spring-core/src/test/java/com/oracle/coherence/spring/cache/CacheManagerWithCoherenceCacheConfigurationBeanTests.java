/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.time.Duration;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringJUnitConfig(CacheManagerWithCoherenceCacheConfigurationBeanTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class CacheManagerWithCoherenceCacheConfigurationBeanTests {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private CoherenceCacheConfiguration coherenceCacheConfiguration;

	@Test
	public void validateCacheConfigurationSettings() throws Exception {

		final Cache cache = this.cacheManager.getCache("foo");

		final CoherenceCache coherenceCache = (CoherenceCache) cache;

		assertThat(coherenceCache.getCacheConfiguration().getTimeToLive())
				.isEqualTo(Duration.ofMillis(4444));
		assertThat(coherenceCache.getCacheConfiguration())
				.isSameAs(this.coherenceCacheConfiguration);
	}

	@Configuration
	@EnableCoherence
	@EnableCaching
	static class Config {
		@Bean
		CoherenceCacheConfiguration coherenceCacheConfiguration() {
			final CoherenceCacheConfiguration cacheConfiguration =
					new CoherenceCacheConfiguration(Duration.ofMillis(4444));
			return cacheConfiguration;
		}
	}
}
