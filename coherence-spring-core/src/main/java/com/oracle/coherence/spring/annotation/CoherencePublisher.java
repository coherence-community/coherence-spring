/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Scope;
import jakarta.inject.Singleton;

import com.oracle.coherence.spring.messaging.CoherencePublisherScanRegistrar;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An introduction advice that automatically implements interfaces and abstract classes and
 * creates {@link com.tangosol.net.topic.Publisher} instances.
 *
 * @author Vaso Putica
 * @see CoherencePublisherScanRegistrar
 * @since 3.0
 */
@Documented
@Retention(RUNTIME)
@Scope
@Singleton
@Target(ElementType.TYPE)
public @interface CoherencePublisher {
	/**
	 * The maximum duration to block synchronous send operations.
	 *
	 * @return The timeout
	 */
	String maxBlock() default "";

	/**
	 * The value may indicate a suggestion for a logical component name,
	 * to be turned into a Spring bean in case of an autodetected component.
	 * @return the suggested component name, if any
	 */
	String name() default "";

	/**
	 * Indicate if {@code default} methods on the interface should be proxied as well.
	 * Note: default methods in JDK classes (such as {@code Function}) can be proxied, but cannot be invoked
	 * via {@code MethodHandle} by an internal Java security restriction for {@code MethodHandle.Lookup}.
	 * @return the boolean flag to proxy default methods or invoke via {@code MethodHandle}.
	 */
	boolean proxyDefaultMethods() default false;

}
