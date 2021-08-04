/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.cache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.oracle.coherence.spring.cache.CoherenceCache;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * @author Gunnar Hillert
 */
@SpringBootTest(classes = CacheAbstractionTests.Config.class)
@ActiveProfiles({"coherenceCacheTests"})
@DirtiesContext
public class CacheAbstractionTests {

	@Autowired
	private CacheManager cacheManager;

	@Test
	public void testCacheExpiration() {
		final Cache cache = this.cacheManager.getCache("foo");
		final CoherenceCache coherenceCache = (CoherenceCache) cache;
		assertThat(coherenceCache.getCacheConfiguration().getTimeToLive()).isEqualTo(Duration.ofMillis(1000));

		cache.put("fookey", "foovalue");
		assertThat(cache.get("fookey").get()).isEqualTo("foovalue");
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(cache.get("fookey")).isNull());
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableCaching
	static class Config {
	}
}

