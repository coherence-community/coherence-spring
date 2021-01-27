/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * A Spring {@link Condition} that matches if the {@link EnableCaching} annotation is specified in the application context.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CachingEnabledCondition implements Condition {

	public CachingEnabledCondition() {
		super();
	}

	@Override
	public boolean matches(
		ConditionContext context,
		AnnotatedTypeMetadata metadata) {

		final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

		if (beanFactory == null) {
			throw new IllegalStateException("BeanFactory is null.");
		}
		else {
			return beanFactory.getBeanNamesForAnnotation(EnableCaching.class).length > 0;
		}
	}

}
