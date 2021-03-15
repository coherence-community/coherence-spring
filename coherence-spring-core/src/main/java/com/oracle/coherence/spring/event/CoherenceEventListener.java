/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>A listener annotation that allows for the subscription of Coherence events.</p>
 * <p>The method will ultimately be wrapped in either an {@link com.tangosol.net.events.EventInterceptor}
 * or a {@link com.tangosol.util.MapListener}.
 * Various qualifier annotations can also be applied to further qualify the types of events and the target event source
 * for a specific listener method. Listener methods can have any name but must take a single parameter that extends either
 * {@link com.tangosol.net.events.Event} or {@link com.tangosol.util.MapEvent} and return {@code void}.</p>
 *
 * <p>For example:</p>
 * <p>The following method will receive a {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent} event every
 * time a map or cache is created or destroyed.</p>
 *
 * <pre><code>
 *  {@literal @}CoherenceEventListener
 *   public void onEvent(CacheLifecycleEvent event) {
 *   }
 * </code></pre>
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CoherenceEventListener {
}
