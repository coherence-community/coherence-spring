/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import java.util.Objects;

import com.oracle.coherence.inject.Injector;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * {@link Injector} to allow the dependency injection of Spring beans
 * into objects being deserialized and manipulated by cache operations.
 *
 * @author Ryan Lubke
 * @since 3.0
 */
public class CoherenceInjector implements Injector {

	@Override
	public void inject(Object target) {
		ApplicationContext ctx = CoherenceContext.getApplicationContext();

		Objects.requireNonNull(ctx, "Unable to local ApplicationContext");

		AutowireCapableBeanFactory factory = ctx.getAutowireCapableBeanFactory();
		factory.autowireBean(target);
	}
}
