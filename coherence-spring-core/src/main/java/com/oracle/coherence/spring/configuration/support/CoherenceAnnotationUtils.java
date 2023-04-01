/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOGGER = LoggerFactory.getLogger(CoherenceAnnotationUtils.class);

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
		LOGGER.debug("Found {} beans in the application context for bean type {}", beanNames.length, beanType.getName());

		final ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();

		for (String beanName : beanNames) {
			final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

			if (beanDefinition instanceof AnnotatedBeanDefinition) {
				final AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
				final Object beanDefinitionMetadataSource = annotatedBeanDefinition.getSource();

				if (beanDefinitionMetadataSource != null && beanDefinitionMetadataSource instanceof MethodMetadata) {
					final MethodMetadata beanMethodMetadata = (MethodMetadata) beanDefinition.getSource();
					boolean foundAnnotation = beanMethodMetadata.getAnnotations().isPresent(annotationType);

					if (foundAnnotation) {
						LOGGER.debug("Found annotation {} on Bean {}.", annotationType.getName(), beanName);
						foundSpringBeans.add(applicationContext.getBean(beanName, beanType));
					}
					else {
						LOGGER.debug("The annotation {} was not found on Bean {}.", annotationType.getName(), beanName);
					}
				}

				AnnotationMetadata classLevelAnnotationMetadata = annotatedBeanDefinition.getMetadata();
				boolean foundAnnotation = classLevelAnnotationMetadata.getAnnotations().isPresent(annotationType);

				if (foundAnnotation) {
					LOGGER.debug("Found class-level annotation {} on Bean {}.", annotationType.getName(), beanName);
					foundSpringBeans.add(applicationContext.getBean(beanName, beanType));
				}
				else {
					LOGGER.debug("The annotation {} was not found on Bean {}.", annotationType.getName(), beanName);
				}
			}
			else {
				LOGGER.debug("Ignoring beanDefinition {} as it is not an AnnotatedBeanDefinition.", beanName);
			}
		}
		return foundSpringBeans;
	}
}
