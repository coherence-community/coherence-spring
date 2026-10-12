/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.boot.mockcache.tests;

import java.util.List;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.data.CoherenceRepositoriesAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration;
import com.oracle.coherence.spring.configuration.CoherenceSpringConfiguration;
import com.oracle.coherence.spring.configuration.NamedCacheConfiguration;
import com.oracle.coherence.spring.doc.boot.mockcache.ProductionApplication;
import com.tangosol.net.Coherence;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static org.assertj.core.api.Assertions.assertThat;

final class MockCacheAssertions {

	private MockCacheAssertions() {
	}

	static void assertNoCoherenceInstances() {
		// Observe only: never close global instances to make these assertions pass.
		assertThat(Coherence.getInstances()).isEmpty();
	}

	static void assertIsolated(ConfigurableApplicationContext context) {
		assertNoCoherenceInstances();
		assertThat(context.isActive()).isTrue();
		assertThat(ConditionEvaluationReport.get(context.getBeanFactory()).getExclusions())
				.containsExactlyInAnyOrder(CoherenceAutoConfiguration.class.getName(),
						CoherenceSpringSessionAutoConfiguration.class.getName(),
						CoherenceRepositoriesAutoConfiguration.class.getName());
		assertThat(context.getBeanNamesForType(PropertySourcesPlaceholderConfigurer.class)).isNotEmpty();
		assertThat(context.containsBean("productionMarker")).isFalse();
		for (Class<?> type : List.of(ProductionApplication.class, CoherenceSpringConfiguration.class,
				NamedCacheConfiguration.class, CoherenceAutoConfiguration.class,
				CoherenceSpringSessionAutoConfiguration.class, CoherenceRepositoriesAutoConfiguration.class,
				Coherence.class, CoherenceServer.class)) {
			assertThat(context.getBeanNamesForType(type, false, false)).as(type.getName()).isEmpty();
		}
	}

}
