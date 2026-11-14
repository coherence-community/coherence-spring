/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies precedence properties with process-scoped Coherence configuration.
 * Each profile needs its own JVM under Surefire's {@code reuseForks=false} policy.
 */
public class CoherencePropertyPrecedenceTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class))
			.withInitializer(new ConfigDataApplicationContextInitializer())
			.withSystemProperties(
					"coherence.localhost=127.0.0.1",
					"coherence.ttl=0",
					"java.net.preferIPv4Stack=true",
					"coherence.wka=127.0.0.1",
					"coherence.cluster=CoherencePropertyPrecedenceTestsCluster"
					)
			.withPropertyValues(
					"coherence.properties.coherence.localhost=127.0.0.1",
					"coherence.properties.coherence.wka=127.0.0.1",
					"coherence.properties.coherence.ttl=0",
					"coherence.properties.coherence.cluster=CoherencePropertyPrecedenceTestsCluster")
			.withInitializer(
					(context) -> {
						ConfigurableConversionService conversionService = new ApplicationConversionService();
						context.getBeanFactory().setConversionService(conversionService);
						context.getEnvironment().setConversionService(conversionService);
					});

	@Test
	public void testCoherencePropertyPrecedenceForSpringEnvironment() {
		this.contextRunner.withUserConfiguration(ConfigWithoutEnableCaching.class)
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
	static class ConfigWithoutEnableCaching {
	}
}
