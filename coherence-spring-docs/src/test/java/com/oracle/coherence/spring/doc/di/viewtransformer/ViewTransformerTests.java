// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.di.viewtransformer;
// end::hide[]

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.net.Coherence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
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
public class ViewTransformerTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withUserConfiguration(SpringConfig.class)
			.withInitializer(new ConfigDataApplicationContextInitializer());

	@Test
	public void testExtractorBindingAnnotation() {
		this.contextRunner.withUserConfiguration(ViewTransformerTests.SpringConfig.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);

			final Map<Long, Person> map = Coherence.getInstance().getSession().getMap("people");
			map.put(1L, new Person("Paul", 50));
			map.put(2L, new Person("Andrea", 36));
			map.put(3L, new Person("Jorge", 32));
			map.put(4L, new Person("Liliana", 40));
			map.put(5L, new Person("Liz", 25));

			final PeopleService service = context.getBean(PeopleService.class);
			service.getPeople();
		});
	}

	@Configuration
	@EnableCoherence
	@ComponentScan
	static class SpringConfig {

	}

}
