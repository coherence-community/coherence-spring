/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceProperties;
import com.oracle.coherence.spring.cache.CoherenceCacheManager;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class))
			.withInitializer(new ConfigDataApplicationContextInitializer());
			;
//
//	@Configuration
//	@EnableCaching
//	static class Config {
//		@Bean
//		CacheManager cacheManager(Coherence coherence) {
//			return new CoherenceCacheManager(coherence);
//		}
//	}

	@Configuration
	@EnableCaching
	//@EnableCoherence
	//@EnableAutoConfiguration(exclude = CacheAutoConfiguration.class)
	//@EnableCoherence
	static class ConfigWithoutCacheManager {

		@Bean
	    public BeanPostProcessor myBeanPostProcessor() {
	        return new MyBeanPostProcessor();
	    }
	}

//	@Test
//	public void testDefaultDataSourceExists() {
//		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutCacheManager.class)
//		.run((context) -> {
//			assertThat(context).hasSingleBean(CoherenceServer.class);
//			assertThat(context).hasSingleBean(CacheManager.class);
//			assertThat(context).getBean(CacheManager.class).isInstanceOf(CoherenceCacheManager.class);
//		});
//	}

	@Test
	public void testCoherenceProperties() {
		this.contextRunner
			.withPropertyValues("spring.config.location", "classpath:testCoherenceProperties.yaml")
			.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutCacheManager.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);
			assertThat(context).hasSingleBean(CacheManager.class);
			assertThat(context).hasSingleBean(CoherenceProperties.class);

			CoherenceProperties coherenceProperties = context.getBean(CoherenceProperties.class);
			assertThat(coherenceProperties.getConfig().toString()).isEqualTo("foo.txt");
			assertThat(context).getBean(CacheManager.class).isInstanceOf(CoherenceCacheManager.class);
		});
	}

	public static class MyBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            System.out.println("before init of: " + beanName);
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            System.out.println("after init of: " + beanName);
            return bean;
        }

    }


//	@Test
//	public void testCacheManagerExists4() {
//		ConfigurableApplicationContext ctx = SpringApplication.run(ConfigWithoutCacheManager.class);
//		CacheManager cm = ctx.getBean(CacheManager.class);
//		System.out.println(">>>>>" + cm);
//	}

//	@Test
//	public void testCacheManagerExists() {
//		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.Config.class)
//		.run((context) -> {
//			assertThat(context).hasSingleBean(CacheManager.class);
//			final CacheManager cacheManager = context.getBean(CacheManager.class);
//			assertThat(cacheManager).isInstanceOf(CoherenceCacheManager.class);
//		});
//	}
//
//	@Test
//	public void testCacheManagerExists2() {
//		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutCacheManager.class)
//		.run((context) -> {
//			assertThat(context).hasSingleBean(CacheManager.class);
//			final CacheManager cacheManager = context.getBean(CacheManager.class);
//			assertThat(cacheManager).isInstanceOf(CoherenceCacheManager.class);
//		});
//	}
//
//	@Test
//	public void testCacheManagerExists3() {
//		this.contextRunner
//		.withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class))
//		.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutCacheManager.class)
//		.run((context) -> {
//			assertThat(context).hasSingleBean(CacheManager.class);
//			final CacheManager cacheManager = context.getBean(CacheManager.class);
//			assertThat(cacheManager).isInstanceOf(CoherenceCacheManager.class);
//		});
//	}



//
//	@Autowired
//	private Coherence coherence;
//
//	@Autowired
//	private CacheManager cacheManager;
//
//	@Test
//	@Order(1)
//	public void getBasicCaches() throws Exception {
//
//		final NamedCache<String, String> fooCache = coherence.getSession().getCache("foo");
//		final NamedCache<String, String> barCache = coherence.getSession().getCache("bar");
//
//		Assertions.assertNotNull(fooCache);
//		Assertions.assertNotNull(barCache);
//
//		Assertions.assertEquals(0, fooCache.size());
//		Assertions.assertEquals(0, barCache.size());
//
//		final Cache springCache = this.cacheManager.getCache("spring");
//		final Cache anotherCache = this.cacheManager.getCache("cache");
//
//		Assertions.assertTrue(springCache instanceof CoherenceCache);
//		Assertions.assertTrue(anotherCache instanceof CoherenceCache);
//
//		final CoherenceCache springCoherenceCache = (CoherenceCache) springCache;
//		final CoherenceCache anotherCoherenceCache = (CoherenceCache) anotherCache;
//
//		Assertions.assertEquals(0, springCoherenceCache.size());
//		Assertions.assertEquals(0, anotherCoherenceCache.size());
//	}
//
//	@Test
//	@Order(2)
//	public void getCacheNames() throws Exception {
//
//		final Collection<String> cacheNames = this.cacheManager.getCacheNames();
//		Assertions.assertEquals(2, cacheNames.size(), "Was expecting 2 Cache Names");
//
//		assertThat(cacheNames, hasItems("spring", "cache"));
//
//	}
//
//	@Test
//	@Order(4)
//	public void getNativeCacheAndCacheStatisticsFromCacheManager() throws Exception {
//
//		final Object nativeCache = this.cacheManager.getCache("spring").getNativeCache();
//
//		Assertions.assertNotNull(nativeCache);
//		Assertions.assertTrue(nativeCache instanceof NamedCache);
//
//		final NamedCache<?, ?> namedCache = (NamedCache<?, ?>) nativeCache;
//
//		Assertions.assertNotNull(namedCache);
//		Assertions.assertEquals(0L, namedCache.size());
//	}
//
//	@Test
//	@Order(5)
//	public void testCachePuts() throws Exception {
//
//		final CoherenceCache springCache = (CoherenceCache) this.cacheManager.getCache("spring");
//
//		springCache.put("foo", "bar");
//		ValueWrapper cacheValue = springCache.get("foo");
//		Assertions.assertEquals(1L, springCache.size());
//		Assertions.assertEquals("bar", cacheValue.get());
//
//		springCache.put("soap", "bar");
//		ValueWrapper cacheValue2 = springCache.get("soap");
//		Assertions.assertEquals(2L, springCache.size());
//		Assertions.assertEquals("bar", cacheValue2.get());
//	}
//
//	@Test
//	@Order(6)
//	public void testCacheSize() throws Exception {
//
//		final CacheManager cacheManager = new CoherenceCacheManager(coherence);
//		final CoherenceCache springCache = (CoherenceCache) cacheManager.getCache("spring");
//
//		Assertions.assertEquals(2L, springCache.size());
//
//		springCache.put("foo2", "bar2");
//		springCache.put("foo3", "bar3");
//
//		Assertions.assertEquals(4, springCache.size());
//
//		springCache.clear();
//
//		Assertions.assertEquals(0, springCache.size());
//	}
//
//	@Test
//	@Order(7)
//	public void testCacheEviction() throws Exception {
//
//		final CacheManager cacheManager = new CoherenceCacheManager(coherence);
//		final CoherenceCache springCache = (CoherenceCache) cacheManager.getCache("spring");
//
//		Assertions.assertEquals(0L, springCache.size());
//
//		springCache.put("Sabal", "minor");
//
//		Assertions.assertEquals(1, springCache.size());
//
//		springCache.evict("Sabal");
//		Assertions.assertEquals(0, springCache.size());
//	}
}
