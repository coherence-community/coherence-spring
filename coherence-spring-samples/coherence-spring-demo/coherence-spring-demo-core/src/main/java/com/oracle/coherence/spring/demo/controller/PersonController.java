/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oracle.coherence.spring.demo.model.Person;
import com.oracle.coherence.spring.demo.service.PersonService;

/**
 * Explicit controller for retrieving People.
 *
 * @author Gunnar Hillert
 *
 */
@RestController
@RequestMapping(path="/api/people")
@Transactional()
public class PersonController {

	@Autowired
	private PersonService personService;

	@GetMapping
	public Page<Person> getPeople(Pageable pageable) {
		return personService.listPeople(pageable);
	}

	@PostMapping
	public Long createPerson(
		@RequestParam("firstName") String firstName,
		@RequestParam("lastName") String lastName,
		@RequestParam("age") int age) {
		return personService.createAndStorePerson(firstName, lastName, age);
	}

	@PostMapping("/{personId}/add-to-event/{eventId}")
	public void addPersonToEvent(
		@PathVariable("personId") Long personId,
		@PathVariable("eventId") Long eventId) {
		personService.addPersonToEvent(personId, eventId);
	}
}
