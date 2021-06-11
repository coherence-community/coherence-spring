/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.annotation.event.Backlog;
import com.oracle.coherence.spring.annotation.event.Connecting;
import com.oracle.coherence.spring.annotation.event.Disconnected;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.federation.FederatedConnectionEvent;

/**
 * Handler for {@link FederatedConnectionEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class FederatedConnectionEventHandler extends FederationEventHandler<FederatedConnectionEvent, FederatedConnectionEvent.Type> {

	public FederatedConnectionEventHandler(MethodEventObserver<FederatedConnectionEvent> observer) {
		super(observer, FederatedConnectionEvent.Type.class, FederatedConnectionEvent::getParticipantName);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Connecting) {
				addType(FederatedConnectionEvent.Type.CONNECTING);
			}
			else if (annotation instanceof Disconnected) {
				addType(FederatedConnectionEvent.Type.DISCONNECTED);
			}
			else if (annotation instanceof Backlog) {
				Backlog backlog = (Backlog) annotation;
				if (backlog.value() == Backlog.Type.EXCESSIVE) {
					addType(FederatedConnectionEvent.Type.BACKLOG_EXCESSIVE);
				}
				else {
					addType(FederatedConnectionEvent.Type.BACKLOG_NORMAL);
				}
			}
			else if (annotation instanceof Error) {
				addType(FederatedConnectionEvent.Type.ERROR);
			}
		}
	}
}
