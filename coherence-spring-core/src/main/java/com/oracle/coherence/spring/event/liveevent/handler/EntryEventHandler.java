/*
 * Copyright (c) 2021, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent.handler;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.Inserting;
import com.oracle.coherence.spring.annotation.event.Removed;
import com.oracle.coherence.spring.annotation.event.Removing;
import com.oracle.coherence.spring.annotation.event.Updated;
import com.oracle.coherence.spring.annotation.event.Updating;
import com.oracle.coherence.spring.event.liveevent.MethodEventObserver;
import com.tangosol.net.events.partition.cache.EntryEvent;

/**
 * Handler for {@link EntryEvent}s.
 *
 * @param <K> the type of the cache keys
 * @param <V> the type of the cache values
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class EntryEventHandler<K, V> extends CacheEventHandler<EntryEvent<K, V>, EntryEvent.Type> {

	private static final Set<EntryEvent.Type> PRE_EVENT_TYPES = Set.of(
			EntryEvent.Type.INSERTING,
			EntryEvent.Type.UPDATING,
			EntryEvent.Type.REMOVING);

	public EntryEventHandler(MethodEventObserver<EntryEvent<K, V>> observer) {
		super(observer, EntryEvent.Type.class);

		for (Annotation annotation : observer.getObservedQualifiers()) {
			if (annotation instanceof Inserting) {
				addType(EntryEvent.Type.INSERTING);
			}
			else if (annotation instanceof Inserted) {
				addType(EntryEvent.Type.INSERTED);
			}
			else if (annotation instanceof Updating) {
				addType(EntryEvent.Type.UPDATING);
			}
			else if (annotation instanceof Updated) {
				addType(EntryEvent.Type.UPDATED);
			}
			else if (annotation instanceof Removing) {
				addType(EntryEvent.Type.REMOVING);
			}
			else if (annotation instanceof Removed) {
				addType(EntryEvent.Type.REMOVED);
			}
		}
	}

	@Override
	boolean isPreEvent(EntryEvent<K, V> event) {
		return PRE_EVENT_TYPES.contains(event.getType());
	}
}
