/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker.model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Model class representing a book.
 * @author Gunnar Hillert
 */
@Entity
@Table(name = "BOOKS")
public class Book implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 * The title of the {@code book}.
	 */
	protected String title;

	/**
	 * The author of the {@code book}.
	 */
	protected String author;

	/**
	 * The number of pages the {@code book} has.
	 */
	protected int pages;

	/**
	 * The {@code book}'s publication date.
	 */
	protected LocalDate published;

	public Book() {
	}

	/**
	 * Constructs a new {@code Book}.
	 *
	 * @param title     the book's title
	 * @param pages     the number of pages the book has
	 * @param author    the book's author
	 * @param published the book's publication date
	 */
	public Book(String title, int pages, String author, LocalDate published) {
		this.title = title;
		this.pages = pages;
		this.author = author;
		this.published = published;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return this.author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getPages() {
		return this.pages;
	}

	public void setPages(int pages) {
		this.pages = pages;
	}

	public LocalDate getPublished() {
		return this.published;
	}

	public void setPublished(LocalDate published) {
		this.published = published;
	}

}
