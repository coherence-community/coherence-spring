/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.oracle.coherence.spring.configuration.DefaultCoherenceConfigurer;
import com.oracle.coherence.spring.event.liveevent.EventHandlerFactory;
import com.oracle.coherence.spring.event.liveevent.handler.EventHandler;
import com.oracle.coherence.spring.event.mapevent.AnnotatedMapListener;
import com.oracle.coherence.spring.event.mapevent.MapListenerRegistrationBean;
import com.oracle.coherence.spring.event.mapevent.MethodMapListener;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.NamedEventInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
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

	private final List<NamedEventInterceptor<?>> interceptors = new ArrayList<>();

	private ApplicationContext applicationContext;

	private MapListenerRegistrationBean mapListenerRegistrationBean;

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

		for (Entry<String, List<Method>> entry : candidates.entrySet()) {
			final String beanName = entry.getKey();
			final List<Method> methods = entry.getValue();

			for (Method method : methods) {
				final Class<?> argumentClassType = method.getParameters()[0].getType();

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Handling Coherence %s - Bean: %s, method: %s",
							argumentClassType.getName(), beanName, method.getName()));
				}
				if (Event.class.isAssignableFrom(argumentClassType)) {
					final Class<? extends Event> eventClassType = (Class<? extends Event>) argumentClassType;
					final EventHandler handler = EventHandlerFactory.create(eventClassType, beanName, method, this.applicationContext);
					final NamedEventInterceptor interceptor = new NamedEventInterceptor(handler.getId(), handler);
					this.interceptors.add(interceptor);
				}
				else {
					// type is MapEvent
					final MethodMapListener listener = new MethodMapListener(beanName, method, this.applicationContext);
					final AnnotatedMapListener mapListener = new AnnotatedMapListener(listener, listener.getObservedQualifiers());
					this.mapListenerRegistrationBean.addMapListener(mapListener);
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

	@Autowired
	public void setMapListenerRegistrationBean(MapListenerRegistrationBean mapListenerRegistrationBean) {
		this.mapListenerRegistrationBean = mapListenerRegistrationBean;
	}
}
