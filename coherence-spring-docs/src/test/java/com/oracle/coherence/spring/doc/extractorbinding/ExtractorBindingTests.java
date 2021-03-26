// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.extractorbinding;
// end::hide[]

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.net.Coherence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class ExtractorBindingTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withConfiguration(AutoConfigurations.of(CacheAutoConfiguration.class))
			.withUserConfiguration(SpringConfig.class)
			.withInitializer(new ConfigDataApplicationContextInitializer());

	@Test
	public void testExtractorBindingAnnotation() {
		this.contextRunner.withUserConfiguration(ExtractorBindingTests.SpringConfig.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);

			final Map<Long, Plant> map = Coherence.getInstance().getSession().getMap("plants");
			map.put(1L, new Plant("Dwarf palmetto", PlantType.PALM, 2));
			map.put(2L, new Plant("Giant Timber Bamboo", PlantType.BAMBOO, 18));
			map.put(3L, new Plant("Date Palm", PlantType.PALM, 22));
			map.put(4L, new Plant("Cavendish", PlantType.BANANA, 3));
			map.put(5L, new Plant("Coconut", PlantType.PALM, 25));

			final PlantService service = context.getBean(PlantService.class);
			service.getPalmTrees();
		});
	}

	@Configuration
	@EnableCoherence
	@ComponentScan
	static class SpringConfig {
	}

}
