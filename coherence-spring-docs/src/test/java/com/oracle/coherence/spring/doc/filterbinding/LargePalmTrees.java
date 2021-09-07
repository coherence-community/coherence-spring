// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.filterbinding;
// end::hide[]
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.oracle.coherence.spring.annotation.FilterBinding;

@FilterBinding                                   // <1>
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface LargePalmTrees {               // <2>
}
