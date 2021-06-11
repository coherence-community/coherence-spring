/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.annotation.event.Committed;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.partition.UnsolicitedCommitEvent;

/**
 * Handler for {@link UnsolicitedCommitEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class UnsolicitedCommitEventHandler extends ServiceEventHandler<UnsolicitedCommitEvent, UnsolicitedCommitEvent.Type> {
	public UnsolicitedCommitEventHandler(MethodEventObserver<UnsolicitedCommitEvent> observer) {
		super(observer, UnsolicitedCommitEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Committed) {
				addType(UnsolicitedCommitEvent.Type.COMMITTED);
			}
		}
	}
}
