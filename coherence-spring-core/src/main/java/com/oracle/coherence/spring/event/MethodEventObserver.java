/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.reflect.Method;

import com.tangosol.net.events.Event;

import org.springframework.util.ReflectionUtils;

/**
 * A Coherence event observer implementation that wraps a {@link Method}.
 *
 * @param <E> the event type
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
class MethodEventObserver<E extends Event<?>> extends BaseMethodObserver {

	/**
	 * Create a {@link MethodEventObserver}.
	 * @param bean to lazily provide the Spring bean that has the executable method
	 * @param method the method to execute when events are received
	 */
	MethodEventObserver(Object bean, Method method) {
		super(bean, method);
	}

	void notify(E event) {
		ReflectionUtils.makeAccessible(this.method); //TODO
		ReflectionUtils.invokeMethod(this.method, this.bean, event);
	}
}
