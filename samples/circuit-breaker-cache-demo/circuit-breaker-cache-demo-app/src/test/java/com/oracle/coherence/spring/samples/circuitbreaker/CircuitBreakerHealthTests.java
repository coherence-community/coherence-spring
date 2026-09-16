/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.health.contributor.Status;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the sample's health reporting without requiring a Coherence server or database.
 */
class CircuitBreakerHealthTests {

	@Test
	void healthEndpointIncludesCircuitBreakerState() {
		new ApplicationContextRunner()
				.withInitializer(new ConfigDataApplicationContextInitializer())
				.withUserConfiguration(HealthConfiguration.class)
				.run((context) -> {
					assertThat(context).hasNotFailed();
					final CircuitBreakerRegistry registry = context.getBean(CircuitBreakerRegistry.class);
					assertThat(registry.find("coherence")).isPresent();
					final CircuitBreaker circuitBreaker = registry.find("coherence").orElseThrow();
					final HealthEndpoint endpoint = context.getBean(HealthEndpoint.class);
					final HealthIndicator indicator = context.getBean("circuitBreakersHealthIndicator", HealthIndicator.class);
					assertThat(endpoint.healthForPath("circuitBreakers")).isNotNull();
					assertThat(endpoint.healthForPath("circuitBreakers").getStatus()).isEqualTo(Status.UP);
					assertThat(((Health) indicator.health().getDetails().get("coherence")).getDetails())
							.containsEntry("state", CircuitBreaker.State.CLOSED);

					try {
						circuitBreaker.transitionToOpenState();
						assertThat(((Health) indicator.health().getDetails().get("coherence")).getDetails())
								.containsEntry("state", CircuitBreaker.State.OPEN);
						assertThat(endpoint.healthForPath("circuitBreakers").getStatus()).isNotEqualTo(Status.UP);
					}
					finally {
						circuitBreaker.reset();
					}
				});
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration(exclude = {CoherenceAutoConfiguration.class, DataSourceAutoConfiguration.class})
	static class HealthConfiguration {
	}
}
