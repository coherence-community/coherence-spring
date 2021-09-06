/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.service;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.oracle.coherence.spring.demo.model.Event;

/**
 *
 * @author Gunnar Hillert
 *
 */
public interface EventService {

	/**
	 * Get a paged list of events.
	 * @param pageable the pagination request
	 * @return the list of events
	 */
	Page<Event> listEvents(Pageable pageable);

	/**
	 * Create a new {@link Event}.
	 * @param title
	 * @param date
	 * @return
	 */
	Event createAndStoreEvent(String title, Date date);

	/**
	 * Get a single {@link Event} for the provided id
	 * @param id the id of the event
	 * @return the event
	 */
	Event getEvent(Long id);

	/**
	 * Remove the {@link Event} from the cache
	 * @param id the id of the event to remove
	 */
	void removeEventFromCache(Long id);

}
