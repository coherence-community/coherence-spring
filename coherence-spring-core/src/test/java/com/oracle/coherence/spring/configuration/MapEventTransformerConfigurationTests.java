/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;

import com.oracle.coherence.spring.annotation.MapEventTransformerBinding;
import com.oracle.coherence.spring.annotation.MapEventTransformerFactory;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(MapEventTransformerConfigurationTests.Config.class)
@DirtiesContext
class MapEventTransformerConfigurationTests {

	@Inject
	@TestTransformer
	MapEventTransformer<String, String, String> transformerOne;

	@Inject
	@TestTransformer("bar")
	MapEventTransformer<String, String, String> transformerTwo;

	@Test
	void shouldInjectCustomTransformer() {
		assertThat(this.transformerOne, is(instanceOf(CustomTransformer.class)));
		assertThat(((CustomTransformer) this.transformerOne).getSuffix(), is("foo"));
		assertThat(this.transformerTwo, is(instanceOf(CustomTransformer.class)));
		assertThat(((CustomTransformer) this.transformerTwo).getSuffix(), is("bar"));
	}

	@Inherited
	@MapEventTransformerBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestTransformer {
		String value() default "foo";
	}

	public static class TestTransformerFactory implements MapEventTransformerFactory<TestTransformer, String, String, String> {
		@Override
		public MapEventTransformer<String, String, String> create(TestTransformer annotation) {
			return new CustomTransformer(annotation.value());
		}
	}

	/**
	 * A custom implementation of a {@link MapEventTransformer}.
	 */
	static class CustomTransformer implements MapEventTransformer<String, String, String> {

		private final String suffix;

		CustomTransformer(String suffix) {
			this.suffix = suffix;
			}

		@Override
		@SuppressWarnings("unchecked")
		public MapEvent<String, String> transform(MapEvent<String, String> event) {
			String sOld = transform(event.getOldValue());
			String sNew = transform(event.getNewValue());
			return new MapEvent<String, String>(event.getMap(), event.getId(), event.getKey(), sOld, sNew);
		}

		String transform(String value) {
			return (value != null) ? value + "-" + this.suffix : null;
		}

		String getSuffix() {
			return this.suffix;
		}
	}

	@Configuration
	@EnableCoherence
	static class Config {
		@Bean
		@TestTransformer
		TestTransformerFactory testTransformerFactory() {
		   return new TestTransformerFactory();
		}
	}
}
