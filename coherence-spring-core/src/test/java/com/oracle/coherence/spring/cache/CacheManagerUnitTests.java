/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.time.Duration;

import com.tangosol.net.Coherence;
import org.junit.jupiter.api.Test;

import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CacheManagerUnitTests {

	@Test
	public void instantiateBasicCacheManager() {
		final CacheManager cacheManager = new CoherenceCacheManager(mock(Coherence.class));

		final CoherenceCacheConfiguration cacheConfiguration = (CoherenceCacheConfiguration) ReflectionTestUtils.getField(cacheManager, "defaultCacheConfiguration");
		assertThat(cacheConfiguration.getTimeToLive()).isEqualTo(Duration.ZERO);
	}

	@Test
	public void instantiateCacheManagerWithCustomDefaultCacheConfiguration() {
		final CoherenceCacheConfiguration cacheConfiguration = new CoherenceCacheConfiguration(Duration.ofMillis(1234));
		final CacheManager cacheManager = new CoherenceCacheManager(mock(Coherence.class), cacheConfiguration);

		final CoherenceCacheConfiguration cacheConfigurationFromCacheManager =
				(CoherenceCacheConfiguration) ReflectionTestUtils.getField(cacheManager, "defaultCacheConfiguration");
		assertThat(cacheConfiguration.getTimeToLive()).isEqualTo(Duration.ofMillis(1234));
	}

	@Test
	public void instantiateCacheManagerWithCoherenceCacheConfiguration() {

		final CoherenceCacheConfiguration cacheConfiguration = new CoherenceCacheConfiguration(Duration.ofMillis(4444));
		final CacheManager cacheManager = new CoherenceCacheManager(mock(Coherence.class), cacheConfiguration);

		final CoherenceCacheConfiguration configuredCacheConfiguration = (CoherenceCacheConfiguration) ReflectionTestUtils.getField(cacheManager, "defaultCacheConfiguration");
		assertThat(configuredCacheConfiguration.getTimeToLive()).isEqualTo(Duration.ofMillis(4444));
	}
}
