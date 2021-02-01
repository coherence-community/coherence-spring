/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
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
	 *
	 * @return
	 */
	Page<Person> listPeople(Pageable pageable);

	/**
	 *
	 * @param firstName
	 * @param lastName
	 * @param age
	 * @return
	 */
	Long createAndStorePerson(String firstName, String lastName, int age);

	/**
	 *
	 * @param personId
	 * @param eventId
	 */
	void addPersonToEvent(Long personId, Long eventId);

}
