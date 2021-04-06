/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tangosol.net.events.Event;
import com.tangosol.util.MapEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * A {@link BeanFactoryPostProcessor} that processes methods annotated with
 * {@literal @}{@link com.oracle.coherence.spring.event.CoherenceEventListener}.
 * Similar to {@link org.springframework.context.event.EventListenerMethodProcessor}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 *
 */
public class CoherenceEventListenerMethodProcessor implements BeanFactoryPostProcessor {
	protected final Log logger = LogFactory.getLog(getClass());

	@Nullable
	private ConfigurableListableBeanFactory beanFactory;

	private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		Assert.state(this.beanFactory != null, "No ConfigurableListableBeanFactory set");
		String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
		for (String beanName : beanNames) {
			if (!ScopedProxyUtils.isScopedTarget(beanName)) {
				Class<?> type = null;
				try {
					type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
				}
				catch (Throwable ex) {
					// An unresolvable bean type, probably from a lazy bean - let's ignore it.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
					}
				}
				if (type != null) {
					if (ScopedObject.class.isAssignableFrom(type)) {
						try {
							Class<?> targetClass = AutoProxyUtils.determineTargetClass(
									beanFactory, ScopedProxyUtils.getTargetBeanName(beanName));
							if (targetClass != null) {
								type = targetClass;
							}
						}
						catch (Throwable ex) {
							// An invalid scoped proxy arrangement - let's ignore it.
							if (this.logger.isDebugEnabled()) {
								this.logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
							}
						}
					}
					try {
						processBean(beanName, type);
					}
					catch (Throwable ex) {
						throw new BeanInitializationException("Failed to process @EventListener " +
								"annotation on bean with name '" + beanName + "'", ex);
					}
				}
			}
		}
		final BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
		final BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(CoherenceEventListenerCandidates.class)
				.addConstructorArgValue(this.coherenceEventListenerCandidateMethods)
				.setLazyInit(true).getBeanDefinition();

		beanDefinitionRegistry.registerBeanDefinition("coherenceEventListenerCandidates", bd);
	}

	private void processBean(final String beanName, final Class<?> targetType) {
		if (!this.nonAnnotatedClasses.contains(targetType) &&
				AnnotationUtils.isCandidateClass(targetType, CoherenceEventListener.class) &&
				!isSpringContainerClass(targetType)) {

			Map<Method, CoherenceEventListener> annotatedMethods = null;
			try {
				final MethodIntrospector.MetadataLookup<CoherenceEventListener> metadataLookup = (method) ->
								AnnotatedElementUtils.findMergedAnnotation(method, CoherenceEventListener.class);
				annotatedMethods = MethodIntrospector.selectMethods(targetType, metadataLookup);
			}
			catch (Throwable ex) {
				// An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
				}
			}

			if (CollectionUtils.isEmpty(annotatedMethods)) {
				this.nonAnnotatedClasses.add(targetType);
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("No @CoherenceEventListener annotations found on bean class: " + targetType.getName());
				}
			}
			else {
				for (Method method : annotatedMethods.keySet()) {
					final List<Parameter> arguments = Arrays.asList(method.getParameters());
					Class<?> argumentClassType = (arguments.size() == 1) ? arguments.get(0).getType() : null;

					if (argumentClassType == null || (!Event.class.isAssignableFrom(argumentClassType)
							&& !MapEvent.class.isAssignableFrom(argumentClassType))) {
						throw new IllegalArgumentException("The @CoherenceEventListener annotated method "
								+ method.getName() + " must have a single Coherence Event or MapEvent argument.");
					}

					this.addEventListenerCandidate(beanName, method);

				}

				if (this.logger.isDebugEnabled()) {
					this.logger.debug(annotatedMethods.size() + " @CoherenceEventListener methods processed on bean '" +
							beanName + "': " + annotatedMethods);
				}
			}
		}
	}

	/**
	 * The keys contain bean names of beans that contain a least one method
	 * annotated with {@link CoherenceEventListener}. The value of the map contains
	 * a list of those annotated methods. Each method is guaranteed to only contain
	 * 1 argument.
	 */
	private final Map<String, List<Method>> coherenceEventListenerCandidateMethods = new HashMap<>();

	private void addEventListenerCandidate(String beanName, Method coherenceListenerMethod) {
		List<Method> beanMethods = this.coherenceEventListenerCandidateMethods.get(beanName);
		if (beanMethods == null) {
			beanMethods = new ArrayList<>();
		}
		beanMethods.add(coherenceListenerMethod);
		this.coherenceEventListenerCandidateMethods.putIfAbsent(beanName, beanMethods);
	}

	/**
	 * Determine whether the given class is an {@code org.springframework}
	 * bean class that is not annotated as a user or test {@link Component}...
	 * which indicates that there is no {@link CoherenceEventListener} to be found there.
	 * @param clazz the class to check
	 * @return true if the class is in the {@code org.springframework.} package and not annotated with {@link Component}
	 */
	private static boolean isSpringContainerClass(Class<?> clazz) {
		return (clazz.getName().startsWith("org.springframework.") &&
				!AnnotatedElementUtils.isAnnotated(ClassUtils.getUserClass(clazz), Component.class));
	}

}
