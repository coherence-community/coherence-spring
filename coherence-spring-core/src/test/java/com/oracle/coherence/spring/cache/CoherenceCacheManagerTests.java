/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.time.Duration;
import java.util.Collection;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringJUnitConfig(CoherenceCacheManagerTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class CoherenceCacheManagerTests {

	@Autowired
	private Coherence coherence;

	@Autowired
	private CacheManager cacheManager;

	@Test
	@Order(1)
	public void getBasicCaches() throws Exception {

		final NamedCache<String, String> fooCache = this.coherence.getSession().getCache("foo");
		final NamedCache<String, String> barCache = this.coherence.getSession().getCache("bar");

		assertThat(fooCache).isNotNull();
		assertThat(barCache).isNotNull();

		assertThat(fooCache).hasSize(0);
		assertThat(barCache).hasSize(0);

		final Cache springCache = this.cacheManager.getCache("spring");
		final Cache anotherCache = this.cacheManager.getCache("cache");

		assertThat(springCache instanceof CoherenceCache).isTrue();
		assertThat(anotherCache instanceof CoherenceCache).isTrue();

		final CoherenceCache springCoherenceCache = (CoherenceCache) springCache;
		final CoherenceCache anotherCoherenceCache = (CoherenceCache) anotherCache;

		final CoherenceCacheConfiguration springCoherenceCacheConfiguration =
				(CoherenceCacheConfiguration) ReflectionTestUtils.getField(springCoherenceCache, "cacheConfiguration");
		final CoherenceCacheConfiguration anotherCoherenceCacheConfiguration =
				(CoherenceCacheConfiguration) ReflectionTestUtils.getField(anotherCoherenceCache, "cacheConfiguration");

		assertThat(springCoherenceCacheConfiguration.getTimeToLive()).isEqualTo(Duration.ZERO);
		assertThat(anotherCoherenceCacheConfiguration.getTimeToLive()).isEqualTo(Duration.ZERO);
		assertThat(springCoherenceCache.size()).isEqualTo(0);
		assertThat(anotherCoherenceCache.size()).isEqualTo(0);
	}

	@Test
	@Order(2)
	public void getCacheNames() throws Exception {

		final Collection<String> cacheNames = this.cacheManager.getCacheNames();
		assertThat(cacheNames).hasSize(2);
		assertThat(cacheNames).contains("spring", "cache");
	}

	@Test
	@Order(4)
	public void getNativeCacheAndCacheStatisticsFromCacheManager() throws Exception {

		final Object nativeCache = this.cacheManager.getCache("spring").getNativeCache();

		assertThat(nativeCache).isNotNull();
		assertThat(nativeCache instanceof NamedCache).isTrue();

		final NamedCache<?, ?> namedCache = (NamedCache<?, ?>) nativeCache;

		assertThat(namedCache).isNotNull();
		assertThat(namedCache).hasSize(0);
	}

	@Test
	@Order(5)
	public void testCachePuts() throws Exception {

		final CoherenceCache springCache = (CoherenceCache) this.cacheManager.getCache("spring");

		springCache.put("foo", "bar");
		ValueWrapper cacheValue = springCache.get("foo");
		assertThat(springCache.size()).isEqualTo(1);
		assertThat("bar").isEqualTo(cacheValue.get());

		springCache.put("soap", "bar");
		ValueWrapper cacheValue2 = springCache.get("soap");
		assertThat(springCache.size()).isEqualTo(2);
		assertThat("bar").isEqualTo(cacheValue2.get());
	}

	@Test
	@Order(6)
	public void testCacheSize() throws Exception {

		final CacheManager cacheManager = new CoherenceCacheManager(this.coherence);
		final CoherenceCache springCache = (CoherenceCache) cacheManager.getCache("spring");

		assertThat(springCache.size()).isEqualTo(2);

		springCache.put("foo2", "bar2");
		springCache.put("foo3", "bar3");

		assertThat(springCache.size()).isEqualTo(4);

		springCache.clear();

		assertThat(springCache.size()).isEqualTo(0);
	}

	@Test
	@Order(7)
	public void testCacheEviction() throws Exception {

		final CacheManager cacheManager = new CoherenceCacheManager(this.coherence);
		final CoherenceCache springCache = (CoherenceCache) cacheManager.getCache("spring");

		assertThat(springCache.size()).isEqualTo(0);

		springCache.put("Sabal", "minor");

		assertThat(springCache.size()).isEqualTo(1);

		springCache.evict("Sabal");
		assertThat(springCache.size()).isEqualTo(0);
	}

	@Configuration
	@EnableCoherence
	@EnableCaching
	static class Config {
		@Bean
		CacheManager cacheManager(Coherence coherence) {
			return new CoherenceCacheManager(coherence);
		}
	}
}
