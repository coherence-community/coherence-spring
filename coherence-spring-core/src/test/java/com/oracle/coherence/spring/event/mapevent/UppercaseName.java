/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.oracle.coherence.spring.annotation.MapEventTransformerBinding;

/**
 * @author Gunnar Hillert
 */
@Inherited
@MapEventTransformerBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface UppercaseName {
}
