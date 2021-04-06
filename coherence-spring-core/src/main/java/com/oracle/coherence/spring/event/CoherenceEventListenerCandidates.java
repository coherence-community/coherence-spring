/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.oracle.coherence.spring.configuration.DefaultCoherenceConfigurer;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.internal.NamedEventInterceptor;
import com.tangosol.util.SafeLinkedList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Holder for Coherence Event Listeners to be configured. See {@link DefaultCoherenceConfigurer}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 *
 */
public class CoherenceEventListenerCandidates implements ApplicationContextAware {

	private static final Log logger = LogFactory.getLog(CoherenceEventListenerCandidates.class);

	private final Map<String, List<Method>> coherenceEventListenerCandidateMethods;

	@SuppressWarnings("unchecked")
	private final List<NamedEventInterceptor<?>> interceptors = new SafeLinkedList();

	private ApplicationContext applicationContext;

	public CoherenceEventListenerCandidates(Map<String, List<Method>> coherenceEventListenerCandidateMethods) {
		super();
		this.coherenceEventListenerCandidateMethods = coherenceEventListenerCandidateMethods;
	}

	public Map<String, List<Method>> getCoherenceEventListenerCandidateMethods() {
		return this.coherenceEventListenerCandidateMethods;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processEventListeners() {
		final Map<String, List<Method>> candidates = this.getCoherenceEventListenerCandidateMethods();
		final MapListenerRegistrationBean mapListenerRegistrationBean = this.applicationContext.getBean(MapListenerRegistrationBean.class);

		for (Entry<String, List<Method>> entry : candidates.entrySet()) {
			final String beanName = entry.getKey();
			final List<Method> methods = entry.getValue();

			for (Method method : methods) {
				final Class<?> argumentClassType = method.getParameters()[0].getType();

				logger.info(String.format("Handling Coherence %s - Bean: %s, method: %s",
					argumentClassType.getName(), beanName, method.getName())); //TODO

				if (Event.class.isAssignableFrom(argumentClassType)) {
					final MethodEventObserver observer = new MethodEventObserver<>(beanName, method, this.applicationContext);
					final EventObserverSupport.EventHandler handler = EventObserverSupport
							.createObserver((Class<? extends Event>) argumentClassType, observer);
					final NamedEventInterceptor interceptor = new NamedEventInterceptor(observer.getId(), handler);
					this.interceptors.add(interceptor);
				}
				else {
					// type is MapEvent
					final MethodMapListener listener = new MethodMapListener(beanName, method, this.applicationContext);
					final AnnotatedMapListener mapListener = new AnnotatedMapListener(listener, listener.getObservedQualifiers());
					mapListenerRegistrationBean.addMapListener(mapListener);
				}
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public List<NamedEventInterceptor<?>> getInterceptors() {
		return this.interceptors;
	}

}
