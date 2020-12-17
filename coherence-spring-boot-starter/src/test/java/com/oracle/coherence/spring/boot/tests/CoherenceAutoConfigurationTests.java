/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceProperties;
import com.oracle.coherence.spring.cache.CoherenceCacheManager;
import com.oracle.coherence.spring.configuration.SessionConfigurationBean;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;

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

	@Configuration
	@EnableCaching
	static class ConfigWithCacheManager {
		@Bean
		CacheManager cacheManager(Coherence coherence) {
			return new CoherenceCacheManager(coherence);
		}
	}

	@Configuration
	@EnableCaching
	static class ConfigWithoutCacheManager {
	}

	@Configuration
	static class ConfigWithoutEnableCaching {
	}

	@Configuration
	@EnableCaching
	static class ConfigWith2SessionConfigurationBeans {
		@Bean
		SessionConfigurationBean sessionConfigurationBeanMySession() {
			final SessionConfigurationBean sessionConfigurationBean =
					new SessionConfigurationBean();
			sessionConfigurationBean.setConfig("test-coherence-config.xml");
			sessionConfigurationBean.setName("MySession");
			return sessionConfigurationBean;
		}

		@Bean
		SessionConfigurationBean sessionConfigurationBeanDefault() {
			final SessionConfigurationBean sessionConfigurationBean =
					new SessionConfigurationBean();
			sessionConfigurationBean.setConfig("test-coherence-config.xml");
			sessionConfigurationBean.setScopeName("fooscope");
			return sessionConfigurationBean;
		}
	}

	@Test
	public void testDefaultCacheManagerExists() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutCacheManager.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);
			assertThat(context).hasSingleBean(CacheManager.class);
			assertThat(context).getBean(CacheManager.class).isInstanceOf(CoherenceCacheManager.class);
		});
	}

	@Test
	public void testThatNoCacheManagerExists() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutCacheManager.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);
			assertThat(context).hasSingleBean(CacheManager.class);
			assertThat(context).getBean(CacheManager.class).isInstanceOf(CoherenceCacheManager.class);
		});
	}

	@Test
	public void testUserProvidedCacheManagerExists() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithCacheManager.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);
			assertThat(context).hasSingleBean(CacheManager.class);
			assertThat(context).getBean(CacheManager.class).isInstanceOf(CoherenceCacheManager.class);
		});
	}

	@Test
	public void testUserProvidedSessionConfigurationBeanExists() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWith2SessionConfigurationBeans.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);
			assertThat(context).hasSingleBean(CacheManager.class);
			assertThat(context).getBean(CacheManager.class).isInstanceOf(CoherenceCacheManager.class);
			assertThat(context).hasBean("sessionConfigurationBeanMySession");
			assertThat(context).hasBean("sessionConfigurationBeanDefault");

			final SessionConfigurationBean sessionConfigurationBean1 = context.getBean("sessionConfigurationBeanMySession", SessionConfigurationBean.class);
			final SessionConfigurationBean sessionConfigurationBean2 = context.getBean("sessionConfigurationBeanDefault", SessionConfigurationBean.class);

			assertThat(sessionConfigurationBean1).isNotNull();
			assertThat(sessionConfigurationBean2).isNotNull();

			assertThat(Coherence.findSession("MySession")).isNotEmpty();
			assertThat(Coherence.findSession(Coherence.DEFAULT_NAME))
				.isNotEmpty().get()
				.extracting(Session::getScopeName).isEqualTo("fooscope");
		});
	}

	@Test
	public void testThatYamlConfiguredSessionConfigurationBeanExists() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutCacheManager.class)
		.withSystemProperties("spring.profiles.active=sessionconfiguration")
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);
			assertThat(context).hasSingleBean(CacheManager.class);
			assertThat(context).getBean(CacheManager.class).isInstanceOf(CoherenceCacheManager.class);

			assertThat(context).hasSingleBean(CoherenceProperties.class);

			final CoherenceProperties coherenceProperties = context.getBean(CoherenceProperties.class);
			assertThat(coherenceProperties.getSessions()).hasSize(2);

			coherenceProperties.getSessions();
			assertThat(context).hasBean("sessionConfigurationBean_1");
			assertThat(context).hasBean("sessionConfigurationBean_2");

			final SessionConfigurationBean sessionConfigurationBean1 = context.getBean("sessionConfigurationBean_1", SessionConfigurationBean.class);
			final SessionConfigurationBean sessionConfigurationBean2 = context.getBean("sessionConfigurationBean_2", SessionConfigurationBean.class);

			assertThat(sessionConfigurationBean1.getName()).isEqualTo("MySession");
			assertThat(sessionConfigurationBean2.getName()).isNull();
			assertThat(Coherence.findSession("MySession")).isNotEmpty();
			assertThat(Coherence.findSession(Coherence.DEFAULT_NAME));

		});
	}
}
