/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A qualifier annotation used when injecting {@link com.tangosol.net.topic.Subscriber}
 * to a {@link com.tangosol.net.topic.NamedTopic} to indicate the name of the
 * subscriber group that the subscriber should belong to.
 *
 * @author Vaso Putica
 * @since 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscriberGroup {

	/**
	 * The name of the subscriber group.
	 * @return the name of the subscriber group
	 */
	String value();
}
