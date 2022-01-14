/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oracle.coherence.spring.demo.dao.PersonRepository;
import com.oracle.coherence.spring.demo.model.Person;
import com.oracle.coherence.spring.demo.service.PersonService;
import org.springframework.util.Assert;

@Transactional
@Service
public class DefaultPersonService implements PersonService {

	@Autowired
	private PersonRepository personRepository;

	@Override
	public Page<Person> listPeople(Pageable pageable) {
		return personRepository.findAll(pageable);
	}

	@Override
	public Long createAndStorePerson(String firstName, String lastName, int age) {
		Assert.hasText(firstName, "firstName must not be null or empty.");
		Assert.hasText(lastName, "lastName must not be null or empty.");
		Assert.isTrue(age >= 0, "The specified age must be a positive number.");
		final Person person = new Person();
		person.setFirstname(firstName);
		person.setLastname(lastName);
		person.setAge(age);
		final Person savedPerson = this.personRepository.save(person);
		return savedPerson.getId();
	}

	@Override
	public void addPersonToEvent(Long personId, Long eventId) {
		// TODO Auto-generated method stub
	}

}
