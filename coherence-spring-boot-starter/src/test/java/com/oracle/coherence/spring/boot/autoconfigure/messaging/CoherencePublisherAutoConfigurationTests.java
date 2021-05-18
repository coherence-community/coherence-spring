/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure.messaging;

import com.oracle.coherence.spring.annotation.CoherencePublisher;
import com.oracle.coherence.spring.annotation.CoherencePublisherScan;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.messaging.publishers.ExamplePublisher;
import com.oracle.coherence.spring.messaging.CoherencePublisherProxyFactoryBean;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Vaso Putica
 *
 */
public class CoherencePublisherAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withInitializer(new ConfigDataApplicationContextInitializer());
	@Test
	void testPublisherAutoConfiguration() {
		this.contextRunner.run((context) -> {
			assertThat(context).hasSingleBean(TestPublisher.class);
			assertThat(context).hasSingleBean(ExamplePublisher.class);
			assertThat(context).hasSingleBean(CoherenceAutoConfiguration.CoherencePublisherScanRegistrarConfiguration.class);
		});
	}

	@Test
	public void testWhenBasePackageConfigured() {
		this.contextRunner.withUserConfiguration(CoherencePublisherAutoConfigurationTests.ConfigWithConfiguredBasePackage.class)
				.run((context) -> {
					assertThat(context).doesNotHaveBean(TestPublisher.class);
					assertThat(context).doesNotHaveBean(CoherenceAutoConfiguration.CoherencePublisherScanRegistrarConfiguration.class);
					assertThat(context).hasSingleBean(ExamplePublisher.class);
					assertThat(context).hasSingleBean(CoherencePublisherProxyFactoryBean.class);
				});
	}

	@Configuration
	@CoherencePublisherScan("com.oracle.coherence.spring.boot.autoconfigure.messaging.publishers")
	static class ConfigWithConfiguredBasePackage {
	}

	@CoherencePublisher
	interface TestPublisher {
	}
}
