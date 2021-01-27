/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link FilterBinding} annotation representing a
 * {@link com.tangosol.util.Filter} produced from a CohQL where clause.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Inherited
@FilterBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface WhereFilter {
	/**
	 * The CohQL query expression.
	 *
	 * @return the CohQL query expression
	 */
	String value();

}
