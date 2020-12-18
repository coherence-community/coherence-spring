/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.namespace;

import com.oracle.coherence.common.base.Classes;

import com.tangosol.coherence.config.ParameterList;
import com.tangosol.coherence.config.SimpleParameterList;
import com.tangosol.config.ConfigurationException;
import com.tangosol.config.expression.ParameterResolver;
import com.tangosol.config.expression.SystemPropertyParameterResolver;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link BeanBuilder}.
 *
 * @author rl
 * @since 3.0
 */
@SpringJUnitConfig(BeanBuilderTest.Config.class)
@DirtiesContext
class BeanBuilderTest {


	@Autowired
	ApplicationContext ctx;
	@Autowired FooBean fooBean;

	@Test
	void shouldRealizeNamedBean() {
		ParameterResolver resolver = new SystemPropertyParameterResolver();
		ClassLoader loader = Classes.getContextClassLoader();

		BeanBuilder builder = new BeanBuilder(ctx, "Foo");
		Boolean result = builder.realizes(FooBean.class, resolver, loader);
		assertThat(result, is(true));
	}

	@Test
	void shouldNotRealizeUnknownBean() {
		ParameterResolver resolver = new SystemPropertyParameterResolver();
		ClassLoader loader = Classes.getContextClassLoader();

		BeanBuilder builder = new BeanBuilder(ctx, "Bar");
		Boolean result = builder.realizes(FooBean.class, resolver, loader);
		assertThat(result, is(false));
	}

	@Test
	void shouldBuildBean() {
		ParameterResolver resolver = new SystemPropertyParameterResolver();
		ClassLoader loader = Classes.getContextClassLoader();
		ParameterList parameters = new SimpleParameterList();

		BeanBuilder builder = new BeanBuilder(ctx, "Foo");
		Object result = builder.realize(resolver, loader, parameters);
		assertThat(result, is(sameInstance(fooBean)));
	}

	@Test
	void shouldNotBuildUnknownBean() {
		ParameterResolver resolver = new SystemPropertyParameterResolver();
		ClassLoader loader = Classes.getContextClassLoader();
		ParameterList parameters = new SimpleParameterList();

		BeanBuilder builder = new BeanBuilder(ctx, "Bar");
		assertThrows(ConfigurationException.class, () -> builder.realize(resolver, loader, parameters));
	}

	@Configuration
	static class Config {
		@Bean(name="Foo")
		FooBean getFooBean() {
			return new FooBean();
		}
	}

	public static class FooBean {
	}
}
