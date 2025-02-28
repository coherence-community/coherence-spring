/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.oracle.coherence.spring.annotation.event.Executed;
import com.oracle.coherence.spring.annotation.event.Executing;
import com.oracle.coherence.spring.annotation.event.Processor;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;

/**
 * Handler for {@link EntryProcessorEvent}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class EntryProcessorEventHandler
		extends CacheEventHandler<EntryProcessorEvent, EntryProcessorEvent.Type> {
	private final Class<?> m_classProcessor;

	private static final Set<EntryProcessorEvent.Type> PRE_EVENT_TYPES = Set.of(EntryProcessorEvent.Type.EXECUTING);

	public EntryProcessorEventHandler(MethodEventObserver<EntryProcessorEvent> observer) {
		super(observer, EntryProcessorEvent.Type.class);

		Class<?> classProcessor = null;

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Processor) {
				classProcessor = ((Processor) annotation).value();
			}
			else if (annotation instanceof Executing) {
				addType(EntryProcessorEvent.Type.EXECUTING);
			}
			else if (annotation instanceof Executed) {
				addType(EntryProcessorEvent.Type.EXECUTED);
			}
		}

		this.m_classProcessor = classProcessor;
	}

	@Override
	boolean shouldFire(EntryProcessorEvent event) {
		return this.m_classProcessor == null || this.m_classProcessor.equals(event.getProcessor().getClass());
	}

	@Override
	boolean isPreEvent(EntryProcessorEvent event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
