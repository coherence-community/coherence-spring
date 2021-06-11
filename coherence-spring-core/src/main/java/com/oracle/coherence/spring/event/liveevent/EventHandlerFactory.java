/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.liveevent;

import java.lang.reflect.Method;

import com.oracle.coherence.spring.event.liveevent.handler.CacheLifecycleEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.CoherenceLifecycleEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.EntryEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.EntryProcessorEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.EventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.FederatedChangeEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.FederatedConnectionEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.FederatedPartitionEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.LifecycleEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.SessionLifecycleEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.TransactionEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.TransferEventHandler;
import com.oracle.coherence.spring.event.liveevent.handler.UnsolicitedCommitEventHandler;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.InterceptorRegistry;
import com.tangosol.net.events.SessionLifecycleEvent;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.federation.FederatedChangeEvent;
import com.tangosol.net.events.federation.FederatedConnectionEvent;
import com.tangosol.net.events.federation.FederatedPartitionEvent;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;
import com.tangosol.net.events.partition.UnsolicitedCommitEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;

import org.springframework.context.ApplicationContext;

/**
 * Manages registration of observer methods with {@link InterceptorRegistry}
 * upon {@link ConfigurableCacheFactory} activation, and their subsequent un-registration on deactivation.
 *
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
public class EventHandlerFactory {

	public static <E extends Event<T>, T extends Enum<T>> EventHandler<E, T> create(
			Class<E> type, String beanName, Method method, ApplicationContext applicationContext) {
		final MethodEventObserver observer = new MethodEventObserver<>(beanName, method, applicationContext);
		return EventHandlerFactory.createEventHandler(type, observer);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static <E extends Event<T>, T extends Enum<T>> EventHandler<E, T> createEventHandler(Class<E> type, MethodEventObserver<E> observer) {
		if (CacheLifecycleEvent.class.equals(type)) {
			return (EventHandler<E, T>) new CacheLifecycleEventHandler(((MethodEventObserver<CacheLifecycleEvent>) observer));
		}
		if (CoherenceLifecycleEvent.class.equals(type)) {
			return (EventHandler<E, T>) new CoherenceLifecycleEventHandler((MethodEventObserver<CoherenceLifecycleEvent>) observer);
		}
		if (EntryEvent.class.equals(type)) {
			return (EventHandler<E, T>) new EntryEventHandler(observer);
		}
		if (EntryProcessorEvent.class.equals(type)) {
			return (EventHandler<E, T>) new EntryProcessorEventHandler((MethodEventObserver<EntryProcessorEvent>) observer);
		}
		if (LifecycleEvent.class.equals(type)) {
			return (EventHandler<E, T>) new LifecycleEventHandler((MethodEventObserver<LifecycleEvent>) observer);
		}
		if (SessionLifecycleEvent.class.equals(type)) {
			return (EventHandler<E, T>) new SessionLifecycleEventHandler((MethodEventObserver<SessionLifecycleEvent>) observer);
		}
		if (TransactionEvent.class.equals(type)) {
			return (EventHandler<E, T>) new TransactionEventHandler((MethodEventObserver<TransactionEvent>) observer);
		}
		if (TransferEvent.class.equals(type)) {
			return (EventHandler<E, T>) new TransferEventHandler((MethodEventObserver<TransferEvent>) observer);
		}
		if (UnsolicitedCommitEvent.class.equals(type)) {
			return (EventHandler<E, T>) new UnsolicitedCommitEventHandler((MethodEventObserver<UnsolicitedCommitEvent>) observer);
		}
		if (FederatedChangeEvent.class.equals(type)) {
			return (EventHandler<E, T>) new FederatedChangeEventHandler((MethodEventObserver<FederatedChangeEvent>) observer);
		}
		if (FederatedConnectionEvent.class.equals(type)) {
			return (EventHandler<E, T>) new FederatedConnectionEventHandler((MethodEventObserver<FederatedConnectionEvent>) observer);
		}
		if (FederatedPartitionEvent.class.equals(type)) {
			return (EventHandler<E, T>) new FederatedPartitionEventHandler((MethodEventObserver<FederatedPartitionEvent>) observer);
		}
		throw new IllegalArgumentException("Unsupported event type: " + type);
	}
}
