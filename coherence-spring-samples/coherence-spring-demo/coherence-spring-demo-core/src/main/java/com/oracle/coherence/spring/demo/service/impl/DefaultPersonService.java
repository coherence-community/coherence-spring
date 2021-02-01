/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
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
		final Person person = new Person();
		person.setFirstname(firstName);
		person.setLastname(lastName);
		person.setAge(age);
		final Person savedPrson = this.personRepository.save(person);
		return savedPrson.getId();
	}

	@Override
	public void addPersonToEvent(Long personId, Long eventId) {
		// TODO Auto-generated method stub
	}

}
