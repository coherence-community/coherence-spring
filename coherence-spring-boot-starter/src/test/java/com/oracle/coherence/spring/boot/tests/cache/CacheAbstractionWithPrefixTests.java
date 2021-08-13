/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.cache;

import com.oracle.coherence.spring.cache.CoherenceCache;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * @author Gunnar Hillert
 */
@SpringBootTest(classes = CacheAbstractionWithPrefixTests.Config.class)
@ActiveProfiles({"coherenceCacheTestsWithPrefix"})
@DirtiesContext
public class CacheAbstractionWithPrefixTests {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private Session session;

	@Test
	public void testCachePrefix() {
		final Cache cache = this.cacheManager.getCache("foo");
		final CoherenceCache coherenceCache = (CoherenceCache) cache;
		assertThat(coherenceCache.getCacheConfiguration().getTimeToLive()).isEqualTo(Duration.ZERO);
		assertThat(coherenceCache.getCacheConfiguration().getCacheNamePrefix()).isEqualTo("FooTesting_");
		assertThat(coherenceCache.getCacheConfiguration().isUseCacheNamePrefix()).isTrue();
		cache.put("fookey", "foovalue");

		final NamedCache<String, String> nativeFooCache = this.session.getCache("foo");
		assertThat(nativeFooCache.size()).isZero();

		final NamedCache<String, String> nativeFooCacheWithPrefix = this.session.getCache("FooTesting_foo");
		assertThat(nativeFooCacheWithPrefix.size()).isOne();

		assertThat(nativeFooCacheWithPrefix.get("fookey")).isEqualTo("foovalue");
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableCaching
	static class Config {
	}
}

