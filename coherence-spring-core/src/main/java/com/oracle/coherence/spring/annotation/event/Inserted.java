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
 * A qualifier annotation used to annotate the parameter {@link com.oracle.coherence.spring.event.CoherenceEventListener}
 * annotated methods that will receive {@link com.tangosol.util.MapEvent MapEvents} of type
 * {@link com.tangosol.util.MapEvent#ENTRY_INSERTED} or that will receive
 * {@link com.tangosol.net.events.partition.cache.EntryEvent EntryEvents} of type
 * {@link com.tangosol.net.events.partition.cache.EntryEvent.Type#INSERTED INSERTED}.
 *
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Inserted {
}
