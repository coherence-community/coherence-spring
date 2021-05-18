/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.oracle.coherence.spring.annotation.Topic;

import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.StringUtils;

/**
 * Annotation utilities.
 *
 * @author Vaso Putica
 * @since 3.0
 */
final class Utils {

	private Utils() {
	}

	static Optional<String> getFirstTopicName(Method method) {
		String[] names = getTopicNames(method);
		return (names.length == 0) ? Optional.empty() : Optional.of(names[0]);
	}

	static String[] getTopicNames(Method method) {
		Set<String> names = new LinkedHashSet<>();
		MergedAnnotations.from(method).stream(Topic.class)
				.forEach((annotation) -> names.add(annotation.getString("value")));

		Class<?> declaringClass = method.getDeclaringClass();
		MergedAnnotations.from(declaringClass).stream(Topic.class)
				.forEach((annotation) -> names.add(annotation.getString("value")));

		return names.stream()
				.filter(StringUtils::hasLength)
				.toArray(String[]::new);
	}
}
