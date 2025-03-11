/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.oracle.coherence.spring.annotation.event.Synced;
import com.oracle.coherence.spring.annotation.event.Syncing;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.federation.FederatedPartitionEvent;

/**
 * Handler for {@link FederatedPartitionEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class FederatedPartitionEventHandler
		extends FederationEventHandler<FederatedPartitionEvent, FederatedPartitionEvent.Type> {

	private static final Set<FederatedPartitionEvent.Type> PRE_EVENT_TYPES = Set.of(FederatedPartitionEvent.Type.SYNCING);

	public FederatedPartitionEventHandler(MethodEventObserver<FederatedPartitionEvent> observer) {
		super(observer, FederatedPartitionEvent.Type.class, FederatedPartitionEvent::getParticipant);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Syncing) {
				addType(FederatedPartitionEvent.Type.SYNCING);
			}
			else if (annotation instanceof Synced) {
				addType(FederatedPartitionEvent.Type.SYNCED);
			}
		}
	}

	@Override
	boolean isPreEvent(FederatedPartitionEvent event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
