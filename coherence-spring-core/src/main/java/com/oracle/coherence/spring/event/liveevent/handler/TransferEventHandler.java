/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.oracle.coherence.spring.annotation.event.Arrived;
import com.oracle.coherence.spring.annotation.event.Assigned;
import com.oracle.coherence.spring.annotation.event.Departed;
import com.oracle.coherence.spring.annotation.event.Departing;
import com.oracle.coherence.spring.annotation.event.Lost;
import com.oracle.coherence.spring.annotation.event.Recovered;
import com.oracle.coherence.spring.annotation.event.Rollback;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;

/**
 * Handler for {@link TransactionEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class TransferEventHandler extends ServiceEventHandler<TransferEvent, TransferEvent.Type> {

	private static final Set<TransferEvent.Type> PRE_EVENT_TYPES = Set.of(
			TransferEvent.Type.DEPARTING);

	public TransferEventHandler(MethodEventObserver<TransferEvent> observer) {
		super(observer, TransferEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Assigned) {
				addType(TransferEvent.Type.ASSIGNED);
			}
			else if (annotation instanceof Arrived) {
				addType(TransferEvent.Type.ARRIVED);
			}
			else if (annotation instanceof Departing) {
				addType(TransferEvent.Type.DEPARTING);
			}
			else if (annotation instanceof Departed) {
				addType(TransferEvent.Type.DEPARTED);
			}
			else if (annotation instanceof Lost) {
				addType(TransferEvent.Type.LOST);
			}
			else if (annotation instanceof Recovered) {
				addType(TransferEvent.Type.RECOVERED);
			}
			else if (annotation instanceof Rollback) {
				addType(TransferEvent.Type.ROLLBACK);
			}
		}
	}

	@Override
	boolean isPreEvent(TransferEvent event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
