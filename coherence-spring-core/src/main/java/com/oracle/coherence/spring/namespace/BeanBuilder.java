/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.namespace;

import java.util.Objects;

import com.tangosol.coherence.config.ParameterList;
import com.tangosol.coherence.config.builder.ParameterizedBuilder;
import com.tangosol.config.ConfigurationException;
import com.tangosol.config.expression.Expression;
import com.tangosol.config.expression.LiteralExpression;
import com.tangosol.config.expression.ParameterResolver;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * Looks up a Spring bean based on the xml {@code <spring:bean>} configuration.
 *
 * @author Jonathan Knight
 * @author Ryan Lubke
 * @since 3.0
 */
public class BeanBuilder implements ParameterizedBuilder<Object>, ParameterizedBuilder.ReflectionSupport {

	/**
	 * The {@link ApplicationContext} used to look-up named beans.
	 */
	private final ApplicationContext context;

	/**
	 * The {@link com.tangosol.config.expression.Expression} that will resolve
	 * to the bean name to look-up.
	 */
	private final Expression<String> beanNameExpression;

	/**
	 * Construct a {@code BeanBuilder} instance.
	 * @param context the {@link ApplicationContext} to use to look-up beans
	 * @param beanNameExpression the expression that will resolve the name of the CDI bean
	 */
	BeanBuilder(ApplicationContext context, String beanNameExpression) {
		this.context = Objects.requireNonNull(context);
		this.beanNameExpression = new LiteralExpression<>(beanNameExpression);
	}

	@Override
	public Object realize(final ParameterResolver parameterResolver, final ClassLoader classLoader,
		final ParameterList parameterList) {

		String beanName = this.beanNameExpression.evaluate(parameterResolver);
		try {
			return this.context.getBean(beanName);
		}
		catch (Exception ex) {
			throw new ConfigurationException(String.format("Cannot resolve bean '%s', ", beanName),
				"Ensure that a bean with that name exists and can be discovered", ex);
		}
	}

	@Override
	public boolean realizes(final Class<?> aClass, final ParameterResolver parameterResolver,
		final ClassLoader classLoader) {

		String beanName = this.beanNameExpression.evaluate(parameterResolver);
		Class<?> resultClz;
		try {
			resultClz = this.context.getType(beanName, false);
			return (resultClz != null && aClass.isAssignableFrom(resultClz));
		}
		catch (BeansException ignored) {
			return false;
		}
	}
}
