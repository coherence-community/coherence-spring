/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AliasFor;

/**
 * Meta-annotation that marks the asynchronous wrapper implementation of Coherence Caches using
 * {@link com.tangosol.net.AsyncNamedCache} for dependency-injection.
 *
 * @author Gunnar Hillert
 * @since 3.0
 *
 * @see com.oracle.coherence.spring.configuration.NamedCacheConfiguration
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NamedCache
@Value("#{" + NamedCacheConfiguration.COHERENCE_ASYNC_CACHE_BEAN_NAME + "}")
public @interface AsyncNamedCache {

	@AliasFor(annotation = Name.class)
	String value() default "";

	@AliasFor(annotation = Name.class, attribute = "value")
	String cacheName() default "";

	@AliasFor(annotation = SessionName.class, attribute = "value")
	String sessionName() default Coherence.DEFAULT_NAME;
}
