/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Provides utilities for retrieving annotations from the provided {@link InjectionPoint}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceAnnotationUtils {

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
}
