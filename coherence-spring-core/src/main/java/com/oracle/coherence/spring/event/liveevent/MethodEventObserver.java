/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent;

import java.lang.reflect.Method;

import com.oracle.coherence.spring.event.BaseMethodObserver;
import com.tangosol.net.events.Event;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

/**
 * A Coherence event observer implementation that wraps a {@link Method}.
 *
 * @param <E> the event type
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
public class MethodEventObserver<E extends Event<?>> extends BaseMethodObserver {

	/**
	 * Create a {@link MethodEventObserver}.
	 * @param beanName provide the Spring bean name that has the executable method
	 * @param method the method to execute when events are received
	 * @param applicationContext the Spring application context to look up the Spring bean
	 */
	public MethodEventObserver(String beanName, Method method, ApplicationContext applicationContext) {
		super(beanName, method, applicationContext);
	}

	public void notify(E event) {
		ReflectionUtils.makeAccessible(this.method); //TODO
		ReflectionUtils.invokeMethod(this.method, this.getTargetBean(), event);
	}
}
