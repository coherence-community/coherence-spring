/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.oracle.coherence.spring.annotation.event.Committed;
import com.oracle.coherence.spring.annotation.event.Committing;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.partition.TransactionEvent;

/**
 * Handler for {@link TransactionEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class TransactionEventHandler extends ServiceEventHandler<TransactionEvent, TransactionEvent.Type> {

	private static final Set<TransactionEvent.Type> PRE_EVENT_TYPES = Set.of(
			TransactionEvent.Type.COMMITTING);

	public TransactionEventHandler(MethodEventObserver<TransactionEvent> observer) {
		super(observer, TransactionEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Committing) {
				addType(TransactionEvent.Type.COMMITTING);
			}
			else if (annotation instanceof Committed) {
				addType(TransactionEvent.Type.COMMITTED);
			}
		}
	}

	@Override
	boolean isPreEvent(TransactionEvent event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
