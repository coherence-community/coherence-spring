/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.coherence.spring.annotation.CoherenceTopicListener;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;


/**
 * A {@link BeanFactoryPostProcessor} that processes classes and methods annotated
 * with {@literal @}{@link CoherenceTopicListener}. Candidate methods will be passed to
 * {@link CoherenceTopicListenerSubscribers} for further processing.
 *
 * @author Vaso Putica
 * @since 3.0
 *
 */
public class CoherenceTopicListenerPostProcessor implements BeanFactoryPostProcessor {
	private static final Log logger = LogFactory.getLog(CoherenceTopicListenerPostProcessor.class);

	private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Assert.state(beanFactory != null, "No ConfigurableListableBeanFactory set");

		for (Iterator<String> beanNamesIt = beanFactory.getBeanNamesIterator(); beanNamesIt.hasNext();) {
			String beanName = beanNamesIt.next();

			if (!ScopedProxyUtils.isScopedTarget(beanName)) {
				Class<?> type = null;
				try {
					type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
				}
				catch (Throwable ex) {
					// An unresolvable bean type, probably from a lazy bean - let's ignore it.
					if (logger.isDebugEnabled()) {
						logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
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
							if (logger.isDebugEnabled()) {
								logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
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
		final BeanDefinition bd = BeanDefinitionBuilder.genericBeanDefinition(CoherenceTopicListenerCandidates.class)
				.addConstructorArgValue(this.coherenceTopicListenerCandidateMethods)
				.setLazyInit(true).getBeanDefinition();

		beanDefinitionRegistry.registerBeanDefinition("coherenceTopicListenerCandidates", bd);
	}

	private void processBean(final String beanName, final Class<?> targetType) {
		if (!this.nonAnnotatedClasses.contains(targetType) &&
				AnnotationUtils.isCandidateClass(targetType, CoherenceTopicListener.class) &&
				!isSpringContainerClass(targetType)) {
			Map<Method, CoherenceTopicListener> annotatedMethods = null;
			try {
				final MethodIntrospector.MetadataLookup<CoherenceTopicListener> metadataLookup = (method) ->
						AnnotationUtils.getAnnotation(method,  CoherenceTopicListener.class);
				annotatedMethods = MethodIntrospector.selectMethods(targetType, metadataLookup);
			}
			catch (Throwable ex) {
				// An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
				if (logger.isDebugEnabled()) {
					logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
				}
			}

			CoherenceTopicListener classAnnotation = AnnotationUtils.getAnnotation(targetType, CoherenceTopicListener.class);
			if (classAnnotation != null) {
				Map<Method, CoherenceTopicListener> notAnnotatedMethods = MethodIntrospector.selectMethods(targetType,
						(MethodIntrospector.MetadataLookup<CoherenceTopicListener>) (method) -> {
							if (!method.isAnnotationPresent(CoherenceTopicListener.class)
									&& method.getParameterCount() == 1) {
								return classAnnotation;
							}
							return null;
						});
				annotatedMethods.putAll(notAnnotatedMethods);
			}

			if (CollectionUtils.isEmpty(annotatedMethods)) {
				this.nonAnnotatedClasses.add(targetType);
				if (logger.isTraceEnabled()) {
					logger.trace("No @CoherenceTopicListener annotations found on bean class: " + targetType.getName());
				}
			}
			else {
				for (Method method : annotatedMethods.keySet()) {
					if (method.getParameterCount() != 1) {
						throw new IllegalArgumentException("The @CoherenceTopicListener annotated method "
								+ method.getName() + " must have a single argument.");
					}
					this.addTopicListenerCandidate(beanName, method);
				}

				if (logger.isDebugEnabled()) {
					logger.debug(annotatedMethods.size() + " @CoherenceTopicListener methods processed on bean '" +
							beanName + "': " + annotatedMethods);
				}
			}
		}
	}

	/**
	 * The keys contain bean names of beans that contain a least one method
	 * annotated with {@link CoherenceTopicListener}. The value of the map contains
	 * a list of those annotated methods. Each method is guaranteed to only contain
	 * 1 argument.
	 */
	private final Map<String, List<Method>> coherenceTopicListenerCandidateMethods = new HashMap<>();

	private void addTopicListenerCandidate(String beanName, Method coherenceTopicListenerMethod) {
		List<Method> beanMethods = this.coherenceTopicListenerCandidateMethods.get(beanName);
		if (beanMethods == null) {
			beanMethods = new ArrayList<>();
		}
		beanMethods.add(coherenceTopicListenerMethod);
		this.coherenceTopicListenerCandidateMethods.putIfAbsent(beanName, beanMethods);
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
