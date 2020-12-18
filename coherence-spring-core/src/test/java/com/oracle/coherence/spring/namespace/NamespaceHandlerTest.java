/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.namespace;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;

import com.tangosol.net.BackingMapContext;
import com.tangosol.net.BackingMapManager;
import com.tangosol.net.BackingMapManagerContext;
import com.tangosol.net.ExtensibleConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheStore;
import com.tangosol.net.cache.ReadWriteBackingMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test for {@link NamespaceHandler}.
 *
 * @author rl
 * @since 3.0
 */
@SpringJUnitConfig(NamespaceHandlerTest.Config.class)
@DirtiesContext
class NamespaceHandlerTest {


	ExtensibleConfigurableCacheFactory eccf;

	@BeforeAll
	static void _beforeAll() {
		System.setProperty("test.bean.name", "TestStoreTwo");
	}

	@BeforeEach
	void setup() {
		String xml = "namespace-handler-test-config.xml";
		ExtensibleConfigurableCacheFactory.Dependencies deps
				= ExtensibleConfigurableCacheFactory.DependenciesHelper.newInstance(xml);
		eccf = new ExtensibleConfigurableCacheFactory(deps);
	}

	@AfterEach
	void cleanup() {
		eccf.dispose();
	}

	@Test
	void shouldInjectBean() {
		Object store = getCacheStore("foo");
		assertThat(store, is(instanceOf(StoreBeanOne.class)));
	}

	@Test
	void shouldInjectOverriddenBean() {
		Object store = getCacheStore("bar");
		assertThat(store, is(instanceOf(StoreBeanTwo.class)));
	}

	Object getCacheStore(String cacheName) {
		NamedCache<Object, Object> cache = eccf.ensureCache(cacheName, null);
		BackingMapManager manager = cache.getCacheService().getBackingMapManager();
		BackingMapManagerContext context = manager.getContext();
		BackingMapContext backingMapContext = context.getBackingMapContext(cache.getCacheName());
		ReadWriteBackingMap backingMap = (ReadWriteBackingMap) backingMapContext.getBackingMap();
		ReadWriteBackingMap.StoreWrapper cacheStore = backingMap.getCacheStore();
		return cacheStore.getStore();
	}

	@Configuration
	@EnableCoherence
	@ComponentScan(basePackages = {"com.oracle.coherence.spring"})
	static class Config {
		@Bean(name="TestStoreOne")
		public <K, V> CacheStore<K, V> getStoreBeanOne() {
			return new StoreBeanOne<>();
		}

		@Bean(name="TestStoreTwo")
		public <K, V> CacheStore<K, V> getStoreBeanTwo() {
			return new StoreBeanTwo<>();
		}
	}

	public static class StoreBeanOne<K, V> implements CacheStore<K, V> {
		@Override
		public void store(K k, V v) {
		}

		@Override
		public void erase(K k) {
		}

		@Override
		public V load(K k) {
			return null;
		}
	}

	public static class StoreBeanTwo<K, V> implements CacheStore<K, V> {
		@Override
		public void store(K k, V v) {
		}

		@Override
		public void erase(K k) {
		}

		@Override
		public V load(K k) {
			return null;
		}
	}
}
