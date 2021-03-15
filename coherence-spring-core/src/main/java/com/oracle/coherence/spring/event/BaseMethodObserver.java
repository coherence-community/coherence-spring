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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oracle.coherence.spring.annotation.event.Synchronous;

/**
 * A Coherence event observer implementation that wraps an {@link Method}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
abstract class BaseMethodObserver {

	protected final Object bean;
	protected final Method method;

	/**
	 * Create a {@link MethodEventObserver}.
	 * @param supplier  a {@link Supplier} to lazily provide the Spring bean that has the executable method
	 * @param method    the method to execute when events are received
	 */
	protected BaseMethodObserver(Object supplier, Method method) {
		this.bean = supplier;
		this.method = method;
	}

	String getId() {
		return this.method.toString();
	}

	Set<Annotation> getObservedQualifiers() {
		return Stream.concat(Arrays.stream(this.method.getParameterAnnotations()[0]),
				Arrays.stream(this.method.getAnnotations()))
				.collect(Collectors.toSet());
	}

	boolean isAsync() {
		return !this.method.isAnnotationPresent(Synchronous.class);
	}
}
