/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.configuration.NamedCacheConfiguration;
import com.tangosol.net.Coherence;

import org.springframework.core.annotation.AliasFor;

/**
 * Meta-annotation that marks the asynchronous wrapper implementation of Coherence Caches using
 * {@link com.tangosol.net.AsyncNamedCache} for dependency-injection.
 *
 * @author Gunnar Hillert
 * @since 3.0
 *
 * @see NamedCacheConfiguration
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@CoherenceAsyncMap
public @interface CoherenceAsyncCache {

	@AliasFor(annotation = Name.class)
	String value() default "";

	@AliasFor(annotation = Name.class, attribute = "value")
	String name() default "";

	@AliasFor(annotation = SessionName.class, attribute = "value")
	String session() default Coherence.DEFAULT_NAME;
}
