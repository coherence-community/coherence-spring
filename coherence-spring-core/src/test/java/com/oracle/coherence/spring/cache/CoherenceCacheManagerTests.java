/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import java.util.Collection;

import org.junit.jupiter.api.Assertions;
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

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringJUnitConfig(CoherenceCacheManagerTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class CoherenceCacheManagerTests {

	@Configuration
	@EnableCoherence
	@EnableCaching
	static class Config {
		@Bean
		CacheManager cacheManager(Coherence coherence) {
			return new CoherenceCacheManager(coherence);
		}
	}

	@Autowired
	private Coherence coherence;

	@Autowired
	private CacheManager cacheManager;

	@Test
	@Order(1)
	public void getBasicCaches() throws Exception {

		final NamedCache<String, String> fooCache = coherence.getSession().getCache("foo");
		final NamedCache<String, String> barCache = coherence.getSession().getCache("bar");

		Assertions.assertNotNull(fooCache);
		Assertions.assertNotNull(barCache);

		Assertions.assertEquals(0, fooCache.size());
		Assertions.assertEquals(0, barCache.size());

		final Cache springCache = this.cacheManager.getCache("spring");
		final Cache anotherCache = this.cacheManager.getCache("cache");

		Assertions.assertTrue(springCache instanceof CoherenceCache);
		Assertions.assertTrue(anotherCache instanceof CoherenceCache);

		final CoherenceCache springCoherenceCache = (CoherenceCache) springCache;
		final CoherenceCache anotherCoherenceCache = (CoherenceCache) anotherCache;

		Assertions.assertEquals(0, springCoherenceCache.size());
		Assertions.assertEquals(0, anotherCoherenceCache.size());
	}

	@Test
	@Order(2)
	public void getCacheNames() throws Exception {

		final Collection<String> cacheNames = this.cacheManager.getCacheNames();
		Assertions.assertEquals(2, cacheNames.size(), "Was expecting 2 Cache Names");

		assertThat(cacheNames, hasItems("spring", "cache"));

	}

	@Test
	@Order(4)
	public void getNativeCacheAndCacheStatisticsFromCacheManager() throws Exception {

		final Object nativeCache = this.cacheManager.getCache("spring").getNativeCache();

		Assertions.assertNotNull(nativeCache);
		Assertions.assertTrue(nativeCache instanceof NamedCache);

		final NamedCache<?, ?> namedCache = (NamedCache<?, ?>) nativeCache;

		Assertions.assertNotNull(namedCache);
		Assertions.assertEquals(0L, namedCache.size());
	}

	@Test
	@Order(5)
	public void testCachePuts() throws Exception {

		final CoherenceCache springCache = (CoherenceCache) this.cacheManager.getCache("spring");

		springCache.put("foo", "bar");
		ValueWrapper cacheValue = springCache.get("foo");
		Assertions.assertEquals(1L, springCache.size());
		Assertions.assertEquals("bar", cacheValue.get());

		springCache.put("soap", "bar");
		ValueWrapper cacheValue2 = springCache.get("soap");
		Assertions.assertEquals(2L, springCache.size());
		Assertions.assertEquals("bar", cacheValue2.get());
	}

	@Test
	@Order(6)
	public void testCacheSize() throws Exception {

		final CacheManager cacheManager = new CoherenceCacheManager(coherence);
		final CoherenceCache springCache = (CoherenceCache) cacheManager.getCache("spring");

		Assertions.assertEquals(2L, springCache.size());

		springCache.put("foo2", "bar2");
		springCache.put("foo3", "bar3");

		Assertions.assertEquals(4, springCache.size());

		springCache.clear();

		Assertions.assertEquals(0, springCache.size());
	}

	@Test
	@Order(7)
	public void testCacheEviction() throws Exception {

		final CacheManager cacheManager = new CoherenceCacheManager(coherence);
		final CoherenceCache springCache = (CoherenceCache) cacheManager.getCache("spring");

		Assertions.assertEquals(0L, springCache.size());

		springCache.put("Sabal", "minor");

		Assertions.assertEquals(1, springCache.size());

		springCache.evict("Sabal");
		Assertions.assertEquals(0, springCache.size());
	}
}
