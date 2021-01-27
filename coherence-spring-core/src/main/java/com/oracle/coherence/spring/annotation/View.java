/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A qualifier annotation used when injecting a cache view.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface View {
	/**
	 * A flag that is {@code true} to cache both the keys and values of the
	 * materialized view locally, or {@code false} to only cache the keys (the
	 * default value is {@code true}).
	 *
	 * @return {@code true} to indicate that values should be cached or
	 *         {@code false} to indicate that only keys should be cached
	 */
	boolean cacheValues() default true;
}
