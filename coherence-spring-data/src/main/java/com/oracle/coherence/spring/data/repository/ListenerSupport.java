/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository;

import com.oracle.coherence.repository.AbstractRepository;
import com.tangosol.util.Filter;

/**
 * Common interface for repositories wishing to expose Coherence-based events.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public interface ListenerSupport<T, ID> {
	/**
	 * Register a listener that will observe all repository events.
	 * @param listener the event listener to register
	 */
	void addListener(AbstractRepository.Listener<? super T> listener);

	/**
	 * Register a listener that will observe all events for a specific entity.
	 * @param id       the identifier of the entity to observe
	 * @param listener the event listener to register
	 */
	void addListener(ID id, AbstractRepository.Listener<? super T> listener);

	/**
	 * Register a listener that will observe all events for entities that
	 * satisfy the specified criteria.
	 * @param filter   the criteria to use to select entities to observe
	 * @param listener the event listener to register
	 */
	void addListener(Filter<?> filter, AbstractRepository.Listener<? super T> listener);

	/**
	 * Create new {@link AbstractRepository.Listener.Builder} instance.
	 * @return a new {@link AbstractRepository.Listener.Builder} instance
	 */
	AbstractRepository.Listener.Builder<T> listener();

	/**
	 * Unregister a listener that observes all repository events.
	 * @param listener the event listener to unregister
	 */
	void removeListener(AbstractRepository.Listener<? super T> listener);

	/**
	 * Unregister a listener that observes all events for a specific entity.
	 * @param id       the identifier of the entity to observe
	 * @param listener the event listener to unregister
	 */
	void removeListener(ID id, AbstractRepository.Listener<? super T> listener);

	/**
	 * Unregister a listener that observes all events for entities that satisfy
	 * the specified criteria.
	 * @param filter   the criteria to use to select entities to observe
	 * @param listener the event listener to unregister
	 */
	void removeListener(Filter<?> filter, AbstractRepository.Listener<? super T> listener);
}
