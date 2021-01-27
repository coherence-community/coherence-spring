/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package data;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class Person
		implements PortableObject, Serializable {
	private String firstName;

	private String lastName;

	private LocalDate dateOfBirth;

	private PhoneNumber phoneNumber;

	/**
	 * Default constructor for serialization.
	 */
	public Person() {
	}

	/**
	 * Create a {@link Person}.
	 *
	 * @param firstName   the person's first name
	 * @param lastName    the person's last name
	 * @param dateOfBirth the person's date of birth
	 * @param phoneNumber the person's phone number
	 */
	public Person(String firstName, String lastName, LocalDate dateOfBirth, PhoneNumber phoneNumber) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		this.phoneNumber = phoneNumber;
	}

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public LocalDate getDateOfBirth() {
		return this.dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public PhoneNumber getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(PhoneNumber phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Person person = (Person) o;
		return Objects.equals(this.firstName, person.firstName)
				&& Objects.equals(this.lastName, person.lastName)
				&& Objects.equals(this.dateOfBirth, person.dateOfBirth);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.firstName, this.lastName, this.dateOfBirth);
	}

	@Override
	public String toString() {
		return "{firstName: \"" + this.firstName + "\""
				+ ", lastName: \"" + this.lastName + "\""
				+ ", dateOfBirth: " + this.dateOfBirth
				+ ", phoneNumber: " + this.phoneNumber
				+ '}';
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		this.firstName = in.readString(0);
		this.lastName = in.readString(1);
		this.dateOfBirth = in.readLocalDate(2);
		this.phoneNumber = in.readObject(3);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeString(0, this.firstName);
		out.writeString(1, this.lastName);
		out.writeDate(2, this.dateOfBirth);
		out.writeObject(3, this.phoneNumber);
	}
}
