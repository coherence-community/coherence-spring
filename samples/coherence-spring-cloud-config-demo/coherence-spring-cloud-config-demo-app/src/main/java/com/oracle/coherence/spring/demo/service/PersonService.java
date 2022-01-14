/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.oracle.coherence.spring.demo.model.Person;

/**
 *
 * @author Gunnar Hillert
 *
 */
public interface PersonService {

	/**
	 *
	 * @return a paged list of people
	 */
	Page<Person> listPeople(Pageable pageable);

	/**
	 *
	 * @param firstName must not be null or empty
	 * @param lastName must not be null or empty
	 * @param age requires positive value
	 * @return the id of the created person
	 */
	Long createAndStorePerson(String firstName, String lastName, int age);

	/**
	 *
	 * @param personId the person to add
	 * @param eventId the event to add the person to
	 */
	void addPersonToEvent(Long personId, Long eventId);

}
