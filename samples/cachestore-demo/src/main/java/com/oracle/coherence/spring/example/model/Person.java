/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.tangosol.io.pof.schema.annotation.Portable;
import com.tangosol.io.pof.schema.annotation.PortableType;

/**
 * A simple representation of a person.
 *
 * @author Jonathan Knight 2021.08.17
 */
@Entity
@Table(name = "PEOPLE")
@PortableType(id = 1000)
public class Person implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The unique identifier for this person.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Portable
	private Long id;

	/**
	 * The age of this person.
	 */
	@Portable
	private int age;

	/**
	 * The person's first name.
	 */
	@Portable
	private String firstname;

	/**
	 * The person's last name.
	 */
	@Portable
	private String lastname;

	/**
	 * The email addresses associated to this person.
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "PERSON_EMAIL_ADDR", joinColumns = @JoinColumn(name = "PERSON_ID"))
	@Column(name = "EMAIL_ADDR")
	@Portable
	private Set<String> emailAddresses = new HashSet<>();

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

	public Set<String> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(Set<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

}
