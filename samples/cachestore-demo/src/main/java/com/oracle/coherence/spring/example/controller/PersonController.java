/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example.controller;

import com.oracle.coherence.spring.configuration.annotation.CoherenceMap;
import com.oracle.coherence.spring.example.model.Person;
import com.tangosol.net.NamedMap;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Explicit controller API for People.
 *
 * @author Jonathan Knight 2021.08.17
 */
// # tag::code[]
@RestController
@RequestMapping(path = "/api/people")
@Transactional()
public class PersonController {

	/**
	 * The {@link NamedMap} to store {@link Person} entities.
	 */
	@CoherenceMap
	private NamedMap<Long, Person> people;

	/**
	 * Create a {@link Person} in the cache.
	 * @param id         the unique identifier for the person
	 * @param firstName  the person's first name
	 * @param lastName   the person's last name
	 * @param age        the person's age
	 * @return the identifier used to create the person
	 */
	@PostMapping
	public Long createPerson(@RequestParam("id") long id, @RequestParam("firstName") String firstName,
			@RequestParam("lastName") String lastName, @RequestParam("age") int age) {
		Person person = new Person();
		person.setFirstname(firstName);
		person.setLastname(lastName);
		person.setAge(age);
		person.setId(id);
		people.put(id, person);
		return id;
	}

	/**
	 * Returns the {@link Person} with the specified identifier.
	 *
	 * @param personId  the unique identifier for the person
	 * @return  the {@link Person} with the specified identifier
	 */
	@GetMapping("/{personId}")
	public Person getPerson(@PathVariable("personId") Long personId) {
		Person person = people.get(personId);
		if (person == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Person " + personId + " does not exist");
		}
		return person;
	}

}
// # end::code[]
