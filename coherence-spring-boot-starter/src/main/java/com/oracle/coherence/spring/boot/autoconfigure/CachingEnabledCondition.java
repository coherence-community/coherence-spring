/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CachingEnabledCondition implements Condition {

	public CachingEnabledCondition() {
		super();
	}

	@Override
	public boolean matches(
		ConditionContext context,
		AnnotatedTypeMetadata metadata) {

		boolean found = context.getBeanFactory().getBeanNamesForAnnotation(EnableCaching.class).length > 0;

		if (found) {
			return true;
		}

		return false;
	}

}
