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
import java.util.Locale;
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
     * The {@code Author}'s address.
     */
    final Address address;

    /**
     * Creates a new Author.
     *
     * @param firstName author's first name
     * @param lastName author's last name
     * @param address author's address
     */
    public Author(final String firstName, final String lastName, final Address address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    /**
     * Return the {@code Author}'s address.
     *
     * @return the {@code Author}'s address
     */
    public Address getAddress() {
        return this.address;
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
     * Return the {@code Author}'s uppercase first name.
     *
     * @return the {@code Author}'s uppercase first name
     */
    public String getUpperFirstName() {
        return this.firstName.toUpperCase(Locale.ROOT);
    }

    /**
     * Return the {@code Author}'s lowercase first name.
     *
     * @return the {@code Author}'s lowercase first name
     */
    public CharSequence getLowerFirstName() {
        return this.firstName.toLowerCase(Locale.ROOT);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Author author = (Author) o;

        if ((this.firstName != null) ? !this.firstName.equals(author.firstName) : author.firstName != null) {
            return false;
        }
        if ((this.lastName != null) ? !this.lastName.equals(author.lastName) : author.lastName != null) {
            return false;
        }
        return (this.address != null) ? this.address.equals(author.address) : author.address == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getAddress());
    }

    @Override
    public String toString() {
        return "Author{" +
                "firstName='" + this.firstName + '\'' +
                ", lastName='" + this.lastName + '\'' +
                ", address='" + this.address + '\'' +
                '}';
    }

    @Override
    public int compareTo(Author o) {
        return this.lastName.compareTo(o.getLastName());
    }
}
