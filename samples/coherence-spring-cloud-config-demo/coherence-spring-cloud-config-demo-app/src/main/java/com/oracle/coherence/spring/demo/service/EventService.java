/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
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
 * Allows for the creation of {@link Event}s.
 * @author Gunnar Hillert
 */
public interface EventService {

	/**
	 * @return a paged list of Events
	 */
	Page<Event> listEvents(Pageable pageable);

	/**
	 *
	 * @param title the title of the event
	 * @param date the date of the even
	 * @return the created event
	 */
	Event createAndStoreEvent(String title, Date date);

	/**
	 *
	 * @param id the id of the event
	 * @return the retrieved event
	 */
	Event getEvent(Long id);

	/**
	 *
	 * @param id if of the event to remove from the cache
	 */
	void removeEventFromCache(Long id);

}
