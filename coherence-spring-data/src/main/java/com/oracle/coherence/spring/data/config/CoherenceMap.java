/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;

/**
 * Optional marker for Coherence repositories where the repository itself
 * should use a cache name different from the name that is auto-selected by
 * the runtime based on the repository class name.
 *
 * @author Ryan Lubke
 * @since 3.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CoherenceMap {
	/**
	 * Specifies the name of the Coherence {@link NamedMap} the annotated repository
	 * should use.
	 * @return the name of the Coherence {@link NamedMap} the annotated repository
	 *         should use
	 */
	String value();

	/**
	 * Specifies the name of the {@link Session} should be used to look up
	 * the {@link NamedMap}.
	 * @return the name of the {@link Session} should be used to look up
	 * 	      the {@link NamedMap}
	 */
	String session() default Coherence.DEFAULT_SCOPE;
}
