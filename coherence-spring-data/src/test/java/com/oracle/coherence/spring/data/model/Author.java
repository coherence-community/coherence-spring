/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.coherence.spring.data.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Entity class representing an Author.
 */
public class Author implements Serializable, Comparable<Author> {

	/**
	 * The {@code Author}'s first name.
	 */
	final String firstName;

	/**
	 * The {@code Author}'s last name.
	 */
	final String lastName;

	/**
	 * Creates a new Author.
	 *
	 * @param firstName author's first name
	 * @param lastName  author's last name
	 */
	public Author(final String firstName, final String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * Return the {@code Author}'s first name.
	 *
	 * @return the {@code Author}'s first name
	 */
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * Return the {@code Author}'s last name.
	 *
	 * @return the {@code Author}'s last name
	 */
	public String getLastName() {
		return this.lastName;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Author author = (Author) o;
		return getFirstName().equals(author.getFirstName()) && getLastName().equals(author.getLastName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFirstName(), getLastName());
	}

	@Override
	public String toString() {
		return "Author{" +
				"firstName='" + this.firstName + '\'' +
				", lastName='" + this.lastName + '\'' +
				'}';
	}

	@Override
	public int compareTo(Author o) {
		return this.lastName.compareTo(o.getLastName());
	}
}
