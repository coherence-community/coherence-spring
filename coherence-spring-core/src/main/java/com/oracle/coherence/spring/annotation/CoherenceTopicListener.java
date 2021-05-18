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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Annotation applied at the class level to indicate that a bean is a Coherence
 * topic {@link com.tangosol.net.topic.Subscriber}.</p>
 *
 * @author Jonathan Knight
 * @since 3.0
 */
@Documented
@Retention(RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface CoherenceTopicListener {
	/**
	 * The {@link CommitStrategy} to use for the subscriber.
	 *
	 * @return The {@link CommitStrategy}
	 */
	CommitStrategy commitStrategy() default CommitStrategy.SYNC;
}
