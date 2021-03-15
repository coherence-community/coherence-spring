/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation.event;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A qualifier annotation used to indicate a specific cache name.
 *
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheName {
    /**
     * Obtain the value used to identify a specific cache.
     *
     * @return value used to identify a specific cache
     */
    String value();
}
