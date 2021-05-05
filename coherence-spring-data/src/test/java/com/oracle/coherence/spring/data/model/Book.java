/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.tangosol.util.UUID;

/**
 * An entity for representing a {@code book}.
 */
@Entity
public class Book implements Cloneable, Serializable {
	/**
	 * The unique id of this book.
	 */
	@Id
	protected final UUID uuid;

	/**
	 * The title of the {@code book}.
	 */
	protected String title;

	/**
	 * The {@link Author author} of the {@code book}.
	 */
	protected Author author;

	/**
	 * The number of pages the {@code book} has.
	 */
	protected int pages;

	/**
	 * The {@code book}'s publication date.
	 */
	protected Calendar published;


	/**
	 * Constructs a new {@code Book}.
	 *
	 * @param title     the book's title
	 * @param pages     the number of pages the book has
	 * @param author    the book's {@link Author author}
	 * @param published the book's publication date
	 */
	public Book(String title, int pages, Author author, Calendar published) {
		this.uuid = new UUID();
		this.title = title;
		this.pages = pages;
		this.author = author;
		this.published = published;
	}

	public Book(UUID uuid) {
		this.uuid = uuid;
		this.title = "UNTITLED";
		this.pages = 0;
		this.author = null;
		this.published = null;
	}

	public Book(Book copy) {
		this.uuid = copy.uuid;
		this.title = copy.title;
		this.pages = copy.pages;
		this.author = copy.author;
		this.published = copy.published;
	}

	public Book() {
		this.uuid = new UUID();
	}

	/**
	 * Return this {@code book}'s unique {@link UUID}.
	 *
	 * @return this {@code book}'s unique {@link UUID}
	 */
	public UUID getUuid() {
		return this.uuid;
	}

	/**
	 * Return this {@code book}'s title.
	 *
	 * @return this {@code book}'s title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Return this {@code book}'s {@link Author author}.
	 *
	 * @return this {@code book}'s {@link Author author}
	 */
	public Author getAuthor() {
		return this.author;
	}

	/**
	 * Returns the number of pages in this {@code book}.
	 *
	 * @return the number of pages in this {@code book}
	 */
	public int getPages() {
		return this.pages;
	}

	/**
	 * Set the number of pages in this {@code book}.
	 *
	 * @param pages the new value for the page count
	 */
	public void setPages(final int pages) {
		this.pages = pages;
	}

	/**
	 * Returns the number of pages in this {@code book} as a long.
	 *
	 * @return the number of pages in this {@code book} as a long
	 */
	public long getPagesAsLong() {
		return this.pages;
	}

	/**
	 * Returns the number of pages in this {@code book} as a double.
	 *
	 * @return the number of pages in this {@code book} as a double
	 */
	public double getPagesAsDouble() {
		return this.pages;
	}

	/**
	 * Returns the number of pages in this {@code book} as a BigDecimal.
	 *
	 * @return the number of pages in this {@code book} as a BigDecimal
	 */
	public BigDecimal getPagesAsBigDecimal() {
		return BigDecimal.valueOf(getPagesAsLong());
	}

	/**
	 * Returns the year this {@code book} was published.
	 *
	 * @return the year this {@code book} was published
	 */
	public int getPublicationYear() {
		return this.published.get(Calendar.YEAR);
	}

	/**
	 * Returns a {@link Calendar} representing the publication date of the {@code book}.
	 *
	 * @return a {@link Calendar} representing the publication date of the {@code book}
	 */
	@SuppressWarnings("unused")
	public Calendar getPublished() {
		return this.published;
	}

	/**
	 * Returns true if page count is greater than 400.
	 *
	 * @return true if page count is greater than 400
	 */
	public boolean isLongBook() {
			return this.getPages() > 400;
	}

	/**
	 * Returns true if page count is less than than 400.
	 *
	 * @return true if page count is less than than 400
	 */
	public boolean isShortBook() {
		return !isLongBook();
	}

	public Collection<String> getChapters() {
		if (this.getTitle().length() % 2 == 0) {
			return Collections.emptySet();
		}
		return Collections.singleton("A");
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Book book = (Book) o;
		return getPages() == book.getPages() && getUuid().equals(book.getUuid()) && getTitle().equals(book.getTitle()) && getAuthor().equals(book.getAuthor());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getUuid(), getTitle(), getAuthor(), getPages());
	}

	@Override
	public String toString() {
		return "Book{" +
				"isbn=" + this.uuid +
				", title='" + this.title + '\'' +
				", author=" + this.author +
				", pages=" + this.pages +
				'}';
	}
}
