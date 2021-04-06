/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.reflect.Method;

import com.tangosol.util.MapEvent;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

/**
 * A {@link BaseMethodObserver} that wraps a map listener {@link Method}.
 *
 * @param <K> the map event key type
 * @param <V> the map event value type
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
class MethodMapListener<K, V> extends BaseMethodObserver {

	/**
	 * Create a {@link MethodEventObserver}.
	 * @param beanName to provide the Spring bean name that has the executable method
	 * @param method the method to execute when events are received
	 * @param applicationContext spring application context to look up the Spring bean
	 */
	MethodMapListener(String beanName, Method method, ApplicationContext applicationContext) {
		super(beanName, method, applicationContext);
	}

	/**
	 * Execute the event using the underlying {@link Method}.
	 * @param event the map event
	 */
	void notify(MapEvent<K, V> event) {
		ReflectionUtils.makeAccessible(this.method); //TODO
		ReflectionUtils.invokeMethod(this.method, this.getTargetBean(), event);
	}
}
