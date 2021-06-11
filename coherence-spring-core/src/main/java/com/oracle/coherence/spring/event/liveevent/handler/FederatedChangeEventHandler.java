/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.annotation.event.CommittingLocal;
import com.oracle.coherence.spring.annotation.event.CommittingRemote;
import com.oracle.coherence.spring.annotation.event.Replicating;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.federation.FederatedChangeEvent;

/**
 * Handler for {@link FederatedChangeEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class FederatedChangeEventHandler
		extends FederationEventHandler<FederatedChangeEvent, FederatedChangeEvent.Type> {

	public FederatedChangeEventHandler(MethodEventObserver<FederatedChangeEvent> observer) {
		super(observer, FederatedChangeEvent.Type.class, FederatedChangeEvent::getParticipant);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof CommittingLocal) {
				addType(FederatedChangeEvent.Type.COMMITTING_LOCAL);
			}
			else if (annotation instanceof CommittingRemote) {
				addType(FederatedChangeEvent.Type.COMMITTING_REMOTE);
			}
			else if (annotation instanceof Replicating) {
				addType(FederatedChangeEvent.Type.REPLICATING);
			}
		}
	}
}
