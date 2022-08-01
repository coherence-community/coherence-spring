/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure.metrics;


import com.oracle.coherence.micrometer.CoherenceMicrometerMetrics;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link CoherenceMicrometerMetrics}.
 *
 * @author Vaso Putica
 * @author Gunnar Hillert
 * @since 3.0
 */
@AutoConfiguration
@ConditionalOnMissingBean(CoherenceMicrometerMetrics.class)
@AutoConfigureAfter({CompositeMeterRegistryAutoConfiguration.class, MetricsAutoConfiguration.class})
@ConditionalOnClass({CoherenceMicrometerMetrics.class, MeterRegistry.class})
@ConditionalOnBean({ MeterRegistry.class })
public class CoherenceMetricsAutoConfiguration {

	@Autowired
	public void bindCoherenceMetricsToRegistry(MeterRegistry meterRegistry) {
		CoherenceMicrometerMetrics.INSTANCE.bindTo(meterRegistry);
	}
}
