/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.enterprise.util.Nonbinding;

/**
 * An annotation used when injecting Coherence resource to indicate a
 * specific resource name.
 *
 * @author Jonathan Knight  2019.10.20
 * @since 20.06
 */
@Documented
@Retention(RUNTIME)
public @interface Name {

	/**
	 * The name used to identify a specific resource.
	 *
	 * @return the name used to identify a specific resource
	 */
	@Nonbinding String value();
}
