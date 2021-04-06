/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oracle.coherence.spring.annotation.event.Synchronous;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * A Coherence event observer implementation that wraps an {@link Method}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
abstract class BaseMethodObserver {

	protected final String beanName;
	protected final Method method;
	private ApplicationContext applicationContext;

	/**
	 * Create a {@link MethodEventObserver}.
	 * @param beanName  the spring bean name that has the executable method
	 * @param method    the method to execute when events are received
	 * @param applicationContext spring application context to look up the Spring bean
	 */
	protected BaseMethodObserver(String beanName, Method method, ApplicationContext applicationContext) {
		this.beanName = beanName;
		this.method = method;
		this.applicationContext = applicationContext;
	}

	/**
	 * Return the target bean instance to use.
	 * @return the actual bean from the {@link ApplicationContext} using {@link #beanName}.
	 */
	protected Object getTargetBean() {
		Assert.notNull(this.applicationContext, "ApplicationContext must no be null");
		return this.applicationContext.getBean(this.beanName);
	}

	protected String getId() {
		return this.method.toString();
	}

	protected Set<Annotation> getObservedQualifiers() {
		return Stream.concat(Arrays.stream(this.method.getParameterAnnotations()[0]),
				Arrays.stream(this.method.getAnnotations()))
				.collect(Collectors.toSet());
	}

	boolean isAsync() {
		return !this.method.isAnnotationPresent(Synchronous.class);
	}
}
