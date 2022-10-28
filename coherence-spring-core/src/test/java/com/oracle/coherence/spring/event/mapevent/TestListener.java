/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.oracle.coherence.common.collections.ConcurrentHashMap;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.annotation.event.CacheName;
import com.oracle.coherence.spring.annotation.event.Deleted;
import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.ScopeName;
import com.oracle.coherence.spring.annotation.event.ServiceName;
import com.oracle.coherence.spring.annotation.event.Synchronous;
import com.oracle.coherence.spring.annotation.event.Updated;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.tangosol.util.MapEvent;
import data.Person;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Gunnar Hillert
 */
public class TestListener {

	private static final Log logger = LogFactory.getLog(TestListener.class);

	private final Map<Integer, Integer> events = new ConcurrentHashMap<>();

	private final List<MapEvent<String, Person>> filteredEvents = Collections.synchronizedList(new ArrayList<>());

	private final List<MapEvent<String, String>> transformedEvents = Collections.synchronizedList(new ArrayList<>());

	private final List<MapEvent<String, String>> transformedWithExtractorEvents = Collections.synchronizedList(new ArrayList<>());

	Integer getEvents(int id) {
		return this.events.get(id);
	}

	public List<MapEvent<String, Person>> getFilteredEvents() {
		return this.filteredEvents;
	}

	public List<MapEvent<String, String>> getTransformedEvents() {
		return this.transformedEvents;
	}

	public List<MapEvent<String, String>> getTransformedWithExtractorEvents() {
		return this.transformedWithExtractorEvents;
	}

	@Synchronous
	@WhereFilter("firstName = 'Bart' and lastName = 'Simpson'")
	@CoherenceEventListener
	void onHomer(@CacheName("people") MapEvent<String, Person> event) {
		this.filteredEvents.add(event);
	}

	@Synchronous
	@CoherenceEventListener
	void onPersonDeleted(@Deleted @CacheName("people") MapEvent<String, Person> event) {
		logger.info("onPersonDeleted: " + event.getKey());
		record(event);
	}

	@Synchronous
	@CoherenceEventListener
	void onPersonInserted(@Inserted @ScopeName("Test") @MapName("people") MapEvent<String, Person> event) {
		logger.info("onPersonInserted: " + event.getKey());
		record(event);
		assertThat(event.getNewValue().getLastName(), is("Simpson"));
	}

	@Synchronous
	@PropertyExtractor("firstName")
	@CoherenceEventListener
	void onPersonInsertedTransformedWithExtractor(@Inserted @MapName("people") MapEvent<String, String> event) {
		this.transformedWithExtractorEvents.add(event);
	}

	@Synchronous
	@UppercaseName
	@CoherenceEventListener
	void onPersonInsertedTransformed(@Inserted @MapName("people") MapEvent<String, String> event) {
		this.transformedEvents.add(event);
	}

	@Synchronous
	@CoherenceEventListener
	void onPersonUpdated(@Updated @ServiceName("StorageService") @MapName("people") MapEvent<String, Person> event) {
		logger.info("onPersonUpdated: " + event.getKey());
		record(event);
		assertThat(event.getOldValue().getLastName(), is("Simpson"));
		assertThat(event.getNewValue().getLastName(), is("SIMPSON"));
	}

	private void record(MapEvent<String, Person> event) {
		this.events.compute(event.getId(), (k, v) -> (v != null) ? v + 1 : 1);
	}
}
