/*
 * Copyright (c) 2013, 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Provides utilities for retrieving annotations from the provided {@link InjectionPoint}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public final class CoherenceAnnotationUtils {

	private static final Log logger = LogFactory.getLog(CoherenceAnnotationUtils.class);

	private CoherenceAnnotationUtils() {
		throw new AssertionError("Utility Class.");
	}

	public static List<Annotation> getAnnotationsMarkedWithMarkerAnnotation(InjectionPoint injectionPoint, Class markerAnnotation) {
		Assert.notNull(injectionPoint, "injectionPoint must not be null.");
		Assert.notNull(markerAnnotation, "markerAnnotation must not be null.");

		final Annotation[] annotations = injectionPoint.getAnnotations();
		final List<Annotation> foundAnnotations = new ArrayList<>();
		for (Annotation annotation : annotations) {
			final Class annotationType = annotation.annotationType();
			if (annotationType.isAnnotationPresent(markerAnnotation)) {
				foundAnnotations.add(annotation);
			}
		}
		return foundAnnotations;
	}

	public static Annotation getSingleAnnotationMarkedWithMarkerAnnotation(InjectionPoint injectionPoint, Class markerAnnotation) {
		final List<Annotation> foundAnnotations = CoherenceAnnotationUtils.getAnnotationsMarkedWithMarkerAnnotation(
				injectionPoint, markerAnnotation);

		if (foundAnnotations.size() > 1) {
			throw new IllegalStateException("More than 1 annotation found: " + StringUtils.collectionToCommaDelimitedString(foundAnnotations));
		}
		else if (foundAnnotations.size() == 1) {
			return foundAnnotations.get(0);
		}
		else {
			return null;
		}
	}

	public static Class<?> getBeanTypeForBeanName(BeanFactory beanFactory, String beanName) {
		final Class<?> beanType = beanFactory.getType(beanName);

		if (beanType == null) {
			throw new IllegalStateException("beanType is not determinable for bean named " + beanName);
		}

		return beanType;
	}

	public static <T> T getSingleBeanWithAnnotation(ApplicationContext applicationContext, final Class<? extends Annotation> annotationType) {
		final Map<String, Object> beans = applicationContext.getBeansWithAnnotation(annotationType);

		if (beans.isEmpty()) {
			throw new IllegalStateException(String.format("No bean annotated with '%s' found.", annotationType.getCanonicalName()));
		}
		else if (beans.size() > 1) {
			throw new IllegalStateException(String.format("Needed 1 but found %s beans annotated with '%s': %s.",
					beans.size(), annotationType.getCanonicalName(), StringUtils.collectionToCommaDelimitedString(beans.keySet())));
		}

		return (T) beans.values().iterator().next();
	}

	public static <T> Collection<T> getBeansOfTypeWithAnnotation(
			ConfigurableApplicationContext applicationContext,
			final Class<? extends T> beanType,
			final Class<? extends Annotation> annotationType) {

		final List<T> foundSpringBeans = new ArrayList<>();

		final String[] beanNames = applicationContext.getBeanNamesForType(beanType);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Found %s beans in the application context for bean type %s", beanNames.length, beanType.getName()));
		}

		final ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

		for (String beanName : beanNames) {
			final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

			if (beanDefinition instanceof AnnotatedBeanDefinition) {
				final AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
				final Object beanDefinitionMetadataSource = annotatedBeanDefinition.getSource();

				if (beanDefinitionMetadataSource != null && beanDefinitionMetadataSource instanceof MethodMetadata
						&& annotatedBeanDefinition.getSource() != null) {
					final MethodMetadata beanMethodMetadata = (MethodMetadata) annotatedBeanDefinition.getSource();
					boolean foundAnnotation = beanMethodMetadata.getAnnotations().isPresent(annotationType);

					if (foundAnnotation) {
						if (logger.isDebugEnabled()) {
							logger.debug(String.format("Found annotation %s on Bean %s.", annotationType.getName(), beanName));
						}
						foundSpringBeans.add(applicationContext.getBean(beanName, beanType));
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug(String.format("The annotation %s was not found on Bean %s.", annotationType.getName(), beanName));
						}
					}
				}

				final AnnotationMetadata classLevelAnnotationMetadata = annotatedBeanDefinition.getMetadata();
				boolean foundAnnotation = classLevelAnnotationMetadata.getAnnotations().isPresent(annotationType);

				if (foundAnnotation) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("Found class-level annotation %s on Bean %s.", annotationType.getName(), beanName));
					}
					foundSpringBeans.add(applicationContext.getBean(beanName, beanType));
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("The annotation %s was not found on Bean %s.", annotationType.getName(), beanName));
					}
				}
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring beanDefinition " + beanName + " as it is not an AnnotatedBeanDefinition.");
				}
			}
		}
		return foundSpringBeans;
	}
}
