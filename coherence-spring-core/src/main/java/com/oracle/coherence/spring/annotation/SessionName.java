/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tangosol.net.Coherence;

/**
 * A qualifier annotation used when injecting Coherence resource to indicate a
 * specific Session name.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionName {
	/**
	 * The name used to identify a specific session.
	 * @return the name used to identify a specific session
	 */
	String value() default Coherence.DEFAULT_NAME;
}
