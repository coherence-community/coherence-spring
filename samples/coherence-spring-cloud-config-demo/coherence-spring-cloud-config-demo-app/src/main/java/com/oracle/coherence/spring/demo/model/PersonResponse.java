/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.model;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JSON response data copied while the person's persistence transaction is active.
 */
public record PersonResponse(Long id, int age, String firstname, String lastname,
		Set<EventResponse> events, Set<String> emailAddresses) {

	/**
	 * Copy both lazy collections so response serialization requires no Hibernate session.
	 * @param person the person to copy within an active transaction
	 * @return the person's response data
	 */
	public static PersonResponse from(Person person) {
		Set<EventResponse> events = person.getEvents().stream()
				.map((event) -> new EventResponse(event.getId(), event.getDate(), event.getTitle()))
				.collect(Collectors.toUnmodifiableSet());
		return new PersonResponse(person.getId(), person.getAge(), person.getFirstname(), person.getLastname(),
				events, Set.copyOf(person.getEmailAddresses()));
	}

	/**
	 * Event data without the ignored participants back-reference.
	 */
	public record EventResponse(Long id, Date date, String title) {
	}
}
