/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceProperties;
import com.oracle.coherence.spring.cache.CoherenceCacheManager;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.support.SpringSystemPropertyResolver;
import com.tangosol.coherence.config.SystemPropertyResolver;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

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
			assertThat(coherenceProperties.getSessions().getServer()).hasSize(2);

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

	@Test
	public void testExistenceOfSingleSpringSystemPropertyResolver() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutEnableCaching.class)
				.run((context) -> {
					assertThat(context).hasSingleBean(SpringSystemPropertyResolver.class);
					SpringSystemPropertyResolver springSystemPropertyResolver = context.getBean(SpringSystemPropertyResolver.class);
					final String propertyPrefix = (String) ReflectionTestUtils.getField(springSystemPropertyResolver, "propertyPrefix");
					assertThat(propertyPrefix).isEqualTo("coherence.properties.");
				});
	}

	@Test
	public void testSpringSystemPropertyResolverForSpringBoot() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutEnableCaching.class)
				.withSystemProperties("spring.profiles.active=coherenceNativePropertiesTests")
				.run((context) -> {
					final SystemPropertyResolver systemPropertyResolver = SystemPropertyResolver.getInstance();
					assertThat(systemPropertyResolver).isNotNull();

					assertThat(systemPropertyResolver.getProperty("coherence.log.limit")).isEqualTo("444");
					assertThat(systemPropertyResolver.getProperty("coherence.log.level")).isEqualTo("1");
					assertThat(systemPropertyResolver.getProperty("coherence.log.logger")).isEqualTo("CoherenceSpring");
					assertThat(systemPropertyResolver.getProperty("coherence.log")).isEqualTo("log4j");
					assertThat(systemPropertyResolver.getProperty("coherence.log.format")).isEqualTo("foobar");

					final SystemPropertyResolver systemPropertyResolverFromSpringContext = context.getBean(SystemPropertyResolver.class);
					assertThat(systemPropertyResolverFromSpringContext).isNotNull();

					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.limit")).isEqualTo("444");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.level")).isEqualTo("1");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.logger")).isEqualTo("CoherenceSpring");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log")).isEqualTo("log4j");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.format")).isEqualTo("foobar");
				});
	}

	@Test
	public void testCoherencePropertyPrecedenceForSpringEnvironment() {
		this.contextRunner.withUserConfiguration(CoherenceAutoConfigurationTests.ConfigWithoutEnableCaching.class)
				.withSystemProperties("spring.profiles.active=coherencePropertiesTests")
				.run((context) -> {
					Environment environment = context.getEnvironment();
					assertThat(environment.getProperty("coherence.properties.coherence.log.limit")).isEqualTo("123");
					assertThat(environment.getProperty("coherence.properties.coherence.log.level")).isEqualTo("5");
					assertThat(environment.getProperty("coherence.properties.coherence.log.logger")).isEqualTo("testing");
					assertThat(environment.getProperty("coherence.properties.coherence.log")).isEqualTo("slf4j");
					assertThat(environment.getProperty("coherence.properties.coherence.log.format")).isEqualTo("Testing: {date}/{uptime} {product} {version} <{level}> (thread={thread}, member={member}): {text}");
				});
	}

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
}
