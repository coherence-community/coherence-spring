/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.oracle.coherence.spring.configuration.CoherenceSpringConfiguration;

import org.springframework.context.annotation.Import;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Main annotation for setting up Coherence within Spring Framework using the {@link CoherenceSpringConfiguration} class.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Import(CoherenceSpringConfiguration.class)
public @interface EnableCoherence {

}
