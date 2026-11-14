/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.tangosol.coherence.config.SystemPropertyResolver;
import com.tangosol.net.CacheFactory;
import com.tangosol.run.xml.XmlElement;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies prefixed properties with process-scoped Coherence configuration.
 * Each profile needs its own JVM under Surefire's {@code reuseForks=false} policy.
 */
public class CoherencePropertyPrefixTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class))
			.withInitializer(new ConfigDataApplicationContextInitializer())
			.withSystemProperties(
					"coherence.localhost=127.0.0.1",
					"coherence.ttl=0",
					"java.net.preferIPv4Stack=true",
					"coherence.wka=127.0.0.1",
					"coherence.cluster=CoherencePropertyPrefixTestsCluster"
					)
			.withPropertyValues(
					"coherence.properties.coherence.localhost=127.0.0.1",
					"coherence.properties.coherence.wka=127.0.0.1",
					"coherence.properties.coherence.ttl=0",
					"coherence.properties.coherence.cluster=CoherencePropertyPrefixTestsCluster")
			.withInitializer(
					(context) -> {
						ConfigurableConversionService conversionService = new ApplicationConversionService();
						context.getBeanFactory().setConversionService(conversionService);
						context.getEnvironment().setConversionService(conversionService);
					});

	@Test
	public void testSpringSystemPropertyResolverForSpringBoot() {
		this.contextRunner.withUserConfiguration(ConfigWithoutEnableCaching.class)
				.withSystemProperties("spring.profiles.active=coherenceNativePropertiesTests")
				.run((context) -> {
					assertThat(context).hasNotFailed();
					assertThat(context).hasSingleBean(CoherenceServer.class);
					assertThat(context.getBean(CoherenceServer.class).isRunning()).isTrue();
					assertThat(CacheFactory.getCluster().isRunning()).isTrue();
					assertThat(CacheFactory.getCluster().getClusterName()).isEqualTo("CoherencePropertyPrefixTestsCluster");

					final XmlElement clusterConfig = CacheFactory.getClusterConfig();
					assertThat(clusterConfig.getSafeElement("unicast-listener/well-known-addresses/address").getString())
							.isEqualTo("127.0.0.1");
					assertThat(clusterConfig.getSafeElement("unicast-listener/address").getString()).isEqualTo("127.0.0.1");
					assertThat(clusterConfig.getSafeElement("multicast-listener/time-to-live").getInt()).isZero();
					assertThat(clusterConfig.getSafeElement("member-identity/cluster-name").getString())
							.isEqualTo("CoherencePropertyPrefixTestsCluster");

					final SystemPropertyResolver systemPropertyResolver = SystemPropertyResolver.getInstance();
					assertThat(systemPropertyResolver).isNotNull();

					assertThat(systemPropertyResolver.getProperty("coherence.wka")).isEqualTo("127.0.0.1");
					assertThat(systemPropertyResolver.getProperty("coherence.localhost")).isEqualTo("127.0.0.1");
					assertThat(systemPropertyResolver.getProperty("coherence.ttl")).isEqualTo("0");
					assertThat(systemPropertyResolver.getProperty("coherence.cluster")).isEqualTo("CoherencePropertyPrefixTestsCluster");

					assertThat(systemPropertyResolver.getProperty("coherence.log.limit")).isEqualTo("0");
					assertThat(systemPropertyResolver.getProperty("coherence.log.level")).isEqualTo("5");
					assertThat(systemPropertyResolver.getProperty("coherence.log.logger")).isEqualTo("CoherenceSpring");
					assertThat(systemPropertyResolver.getProperty("coherence.log")).isEqualTo("jdk");
					assertThat(systemPropertyResolver.getProperty("coherence.log.format"))
							.isEqualTo("foobar {date}/{uptime} <{level}> (thread={thread}, member={member}): {text}");

					final SystemPropertyResolver systemPropertyResolverFromSpringContext = context.getBean(SystemPropertyResolver.class);
					assertThat(systemPropertyResolverFromSpringContext).isNotNull();

					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.limit")).isEqualTo("0");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.level")).isEqualTo("5");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.logger")).isEqualTo("CoherenceSpring");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log")).isEqualTo("jdk");
					assertThat(systemPropertyResolverFromSpringContext.getProperty("coherence.log.format"))
							.isEqualTo("foobar {date}/{uptime} <{level}> (thread={thread}, member={member}): {text}");
				});
	}

	@Configuration
	static class ConfigWithoutEnableCaching {
	}
}
