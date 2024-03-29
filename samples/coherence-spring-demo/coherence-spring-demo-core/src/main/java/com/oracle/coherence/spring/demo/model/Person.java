/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 *
 * @author Gunnar Hillert
 *
 */
@Entity
@Table(name="PEOPLE")
public class Person implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private int age;
	private String firstname;
	private String lastname;

	@ManyToMany(targetEntity = Event.class)
	@JsonIgnore
	private Set<Event> events = new HashSet<>();

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "PERSON_EMAIL_ADDR", joinColumns = @JoinColumn(name = "PERSON_ID"))
	@Column(name = "EMAIL_ADDR")
	private Set<String> emailAddresses = new HashSet<>();

	public Person() {
	}

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

	public Set<Event> getEvents() {
		return events;
	}

	protected void setEvents(Set<Event> events) {
		this.events = events;
	}

	public void addToEvent(Event event) {
		this.getEvents().add(event);
		event.getParticipants().add(this);
	}

	public void removeFromEvent(Event event) {
		this.getEvents().remove(event);
		event.getParticipants().remove(this);
	}

	public Set<String> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(Set<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

}
