// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.di.wherefilter;
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
public class WhereFilterTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withUserConfiguration(SpringConfig.class)
			.withInitializer(new ConfigDataApplicationContextInitializer());

	@Test
	public void testExtractorBindingAnnotation() {
		this.contextRunner.withUserConfiguration(SpringConfig.class)
		.run((context) -> {
			assertThat(context).hasSingleBean(CoherenceServer.class);

			final Map<Long, Person> map = Coherence.getInstance().getSession().getMap("people");
			map.put(1L, new Person("Homer", "Simpson", 36));
			map.put(2L, new Person("Marge", "Simpson", 34));
			map.put(3L, new Person("Bart", "Simpson",10));
			map.put(4L, new Person("Lisa", "Simpson",8));
			map.put(5L, new Person("Maggie", "Simpson",1));
			map.put(6L, new Person("Peter", "Griffin", 42));
			map.put(7L, new Person("Lois", "Griffin", 40));
			map.put(8L, new Person("Meg", "Griffin", 16));
			map.put(9L, new Person("Chris", "Griffin", 13));
			map.put(10L, new Person("Stewie", "Griffin", 1));
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
