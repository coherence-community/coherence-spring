/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import java.util.Map;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheLoader;
import com.tangosol.net.cache.LocalCache;
import com.tangosol.util.ExternalizableHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SpringNamespaceHandler}.
 *
 * @author Brian Oliver
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(SpringApplicationConfig.class)
public class SpringApplicationTests {
	/**
	 * The {@link ApplicationContext} we'll use for testing.
	 */
	@Autowired
	protected ApplicationContext context;

	/**
	 * The {@link SpringBasedCoherenceSession} we'll use for testing.
	 */
	@Autowired
	protected SpringBasedCoherenceSession session;

	/**
	 * Stop the cluster after each test have executed.
	 */
	@AfterAll
	public static void stopCluster() {
		CacheFactory.shutdown();
	}


	/**
	 * Test the use of Spring to inject a CacheStore.
	 */
	@Test
	public void testCacheStore() {
		String[] asCacheNames = new String[] {"CacheStore", "CacheStorePull"};

		for (String sCacheName : asCacheNames) {
			NamedCache cache = this.session.getCache(sCacheName);

			// the CacheStore provided by Spring is an instance of MapCacheStore
			// which has an internal map that contains the entry <"key", "value">
			assertThat("value").isEqualTo(cache.get("key"));

			// this asserts that the {cache-name} macro succeeded in injecting
			// the cache name to the cache store (see StubNamedCacheStore)
			assertThat(sCacheName).isEqualTo(cache.get(StubNamedCacheStore.CACHE_NAME_KEY));
		}

		BeanFactory beanFactory = this.session.getResourceRegistry().getResource(BeanFactory.class);
		StubNamedCacheStore cs = beanFactory.getBean("mapCacheStorePull", StubNamedCacheStore.class);

		assertThat(cs.getSpelValue()).isEqualTo("Prosper");
	}


	/**
	 * Test the injection of a backing map manager context via
	 * the {manager-context} macro.
	 */
	@Test
	public void testBackingMapManagerContextInjection() {
		String[] asCacheNames = new String[] {"CacheBML", "CacheBMLPull"};
		String[] asBeanNames  = new String[] {"bml", "bmlPull"};

		for (int i = 0; i < asCacheNames.length; ++i) {
			NamedCache             cache       = this.session.getCache(asCacheNames[i]);
			BeanFactory            beanFactory = this.session.getResourceRegistry().getResource(BeanFactory.class);
			StubBackingMapListener bml         = beanFactory.getBean(asBeanNames[i], StubBackingMapListener.class);

			assertThat(bml.isContextConfigured()).isFalse();
			cache.put("key", "value");
			assertThat(bml.isContextConfigured()).isTrue();
		}
	}


	/**
	 * Test the registration of a bean factory and injection of a backing map.
	 */
	@Test
	public void testManualRegistration() {
		// this local cache will be used as a backing map
		LocalCache localCache = new LocalCache(100, 0, new AbstractCacheLoader() {
			@Override
			public Object load(Object oKey) {
				return ExternalizableHelper.toBinary("mock");
			}
		});

		// instead of creating a Spring application context, create
		// a simple mock BeanFactory that returns the local cache
		// created above
		BeanFactory factory = mock(BeanFactory.class);

		when(factory.getBean("localBackingMap")).thenReturn(localCache);

		// register the mock BeanFactory with the cache factory so that
		// it is used as the backing map (see the cache config file)
		this.session.getResourceRegistry().registerResource(BeanFactory.class, "mock", factory);

		NamedCache namedCache = this.session.getCache("CacheCustomBackingMap");

		// cache loader always returns the same value
		assertThat("mock").isEqualTo(namedCache.get("key"));

		// assert backing map properties
		Map mapBacking =
			namedCache.getCacheService().getBackingMapManager().getContext()
				.getBackingMapContext("CacheCustomBackingMap").getBackingMap();

		assertThat(LocalCache.class).isEqualTo(mapBacking.getClass());
		assertThat(100).isEqualTo(((LocalCache) mapBacking).getHighUnits());
		assertThat(localCache).isEqualTo(mapBacking);
	}


	/**
	 * Test interceptor configuration.
	 */
	@Test
	public void testInterceptor() {
		StubInterceptor interceptor = this.context.getBean(StubInterceptor.class);

		assertThat(interceptor.eventReceived()).isFalse();
		this.session.getCache("CacheInterceptor").put("key", "value");
		assertThat(interceptor.eventReceived()).isTrue();
	}
}
