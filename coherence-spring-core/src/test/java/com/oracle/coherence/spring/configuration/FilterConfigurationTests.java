/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import com.oracle.coherence.spring.annotation.AlwaysFilter;
import com.oracle.coherence.spring.annotation.FilterBinding;
import com.oracle.coherence.spring.annotation.FilterFactory;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.util.Filter;
import com.tangosol.util.QueryHelper;
import com.tangosol.util.filter.AllFilter;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
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
@SpringJUnitConfig(FilterConfigurationTests.Config.class)
@DirtiesContext
class FilterConfigurationTests {

	@Autowired
	ConfigurableApplicationContext ctx;

	@Inject
	@AlwaysFilter
	Filter<?> filterOne;

	@Inject
	@WhereFilter("foo=1")
	Filter<?> filterTwo;

	@Inject
	@CustomFilter("testing")
	Filter<?> filterThree;

	@Inject
	@CustomFilterTwo("four")
	Filter<?> filterFour;

	@Inject
	@CustomFilterTwo("five.1")
	@CustomFilterTwo("five.2")
	Filter<?> filterFive;

	@FilterBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface CustomFilter {
		String value();
	}

	@FilterBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface CustomFilterTwoHolder {

		// Dummy field - without it, the annotation does not work with Spring
		String can_be_anything() default "";
		CustomFilterTwo[] value() default {};
	}

	@FilterBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Repeatable(CustomFilterTwoHolder.class)
	public @interface CustomFilterTwo {
		String value();
	}

	@Test
	void shouldInjectAlwaysFilter() {
		assertThat(this.filterOne, is(instanceOf(com.tangosol.util.filter.AlwaysFilter.class)));
	}

	@Test
	void shouldInjectWhereFilter() {
		Filter<?> expected = QueryHelper.createFilter("foo=1");
		assertThat(this.filterTwo, is(expected));
	}

	@Test
	void shouldInjectCustomFilter() {
		assertThat(this.filterThree, is(instanceOf(FilterStub.class)));
		assertThat(((FilterStub<?>) this.filterThree).getValue(), is("testing"));
	}

	@Test
	void shouldInjectRepeatebleQualifiedFilter() {
		assertThat(this.filterFour, is(instanceOf(FilterStub.class)));
		assertThat(((FilterStub<?>) this.filterFour).getValue(), is("four"));
	}

	@Test
	void shouldInjectRepeatableHolderQualifiedFilter() {
		assertThat(this.filterFive, is(instanceOf(AllFilter.class)));
		final Filter<?>[] filters = ((AllFilter) this.filterFive).getFilters();
		assertThat(filters.length, is(2));
		assertThat(filters[0], is(instanceOf(FilterStub.class)));
		assertThat(((FilterStub<?>) filters[0]).getValue(), is("five.1"));
		assertThat(filters[1], is(instanceOf(FilterStub.class)));
		assertThat(((FilterStub<?>) filters[1]).getValue(), is("five.2"));
	}

	static class FilterStub<T> implements Filter<T> {

		private final String value;

		FilterStub(String value) {
			this.value = value;
		}

		@Override
		public boolean evaluate(T o) {
			return true;
		}

		String getValue() {
			return this.value;
		}
	}

	@Configuration
	@EnableCoherence
	static class Config {

		@Bean
		@CustomFilter("")
		FilterFactory<CustomFilter, ?> customFactory() {
			return (annotation) -> new FilterStub<>(annotation.value());
		}

		@Bean
		@CustomFilterTwo("")
		FilterFactory<CustomFilterTwo, ?> customFactoryTwo() {
			return (annotation) -> new FilterStub<>(annotation.value());
		}

		@Bean
		@CustomFilterTwoHolder
		FilterFactory<CustomFilterTwoHolder, ?> customFilterTwoHolder() {
			return (annotation) -> {
				Filter[] filters = Arrays.stream(annotation.value())
					.map((ann) -> new FilterStub<>(ann.value()))
					.toArray(FilterStub[]::new);
				return (filters.length == 1) ? filters[0] : new AllFilter(filters);
			};
		}
	}
}
