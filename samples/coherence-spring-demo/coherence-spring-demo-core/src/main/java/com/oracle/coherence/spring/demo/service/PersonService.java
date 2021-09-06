/*
 * Copyright (c) 2020, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
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
	 * @param pageable the pagination request
	 * @return a paged list of people
	 */
	Page<Person> listPeople(Pageable pageable);

	/**
	 *
	 * @param firstName must not be empty
	 * @param lastName must not be empty
	 * @param age must be positive
	 * @return the id of the saved person
	 */
	Long createAndStorePerson(String firstName, String lastName, int age);

	/**
	 * Add a person to an event.
	 * @param personId the id of the person to add
	 * @param eventId the id of the even to add the person to
	 */
	void addPersonToEvent(Long personId, Long eventId);

	/**
	 * Return a person for the provided personId
	 * @param personId id of the person
	 * @return the person
	 */
	Person getPerson(Long personId);
}
