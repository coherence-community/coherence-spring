/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A simple representation of a person.
 *
 * @author Jonathan Knight 2021.08.17
 */
// # tag::person[]
@Entity
@Table(name = "PEOPLE")
public class Person implements Serializable {

	/**
	 * The unique identifier for this person.
	 */
	@Id
	private Long id;

	/**
	 * The age of this person.
	 */
	private int age;

	/**
	 * The person's first name.
	 */
	private String firstname;

	/**
	 * The person's last name.
	 */
	private String lastname;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

}
// # end::person[]
