/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker.service;

import java.util.List;

import com.oracle.coherence.spring.samples.circuitbreaker.model.Book;

/**
 * A service to manage books.
 * @author Gunnar Hillert
 */
public interface BookService {

	/**
	 * Get a paged list of events.
	 * @return the list of events
	 */
	List<Book> getAllBooks();

	/**
	 * Create a new {@link Book}.
	 * @param book the book to add
	 * @return the added book
	 */
	Book addBook(Book book);

	/**
	 * Get a single {@link Book} for the provided id.
	 * @param id the id of the book
	 * @return the book
	 */
	Book getBook(Long id);

	/**
	 * Remove the {@link Book} from the cache.
	 * @param id the id of the event to remove
	 */
	void removeBookFromCache(Long id);

}
