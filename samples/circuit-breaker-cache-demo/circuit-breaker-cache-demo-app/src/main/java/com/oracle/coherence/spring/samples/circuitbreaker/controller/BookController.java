/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker.controller;

import java.util.List;

import com.oracle.coherence.spring.samples.circuitbreaker.model.Book;
import com.oracle.coherence.spring.samples.circuitbreaker.service.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for handling {@link Book}s.
 * @author Gunnar Hillert
 */
@RestController
@RequestMapping(path = "/api/books")
public class BookController {

	@Autowired
	private BookService bookService;

	@GetMapping
	public List<Book> getBooks() {
		return this.bookService.getAllBooks();
	}

	@GetMapping("/{id}")
	public Book getBook(@PathVariable Long id) {
		return this.bookService.getBook(id);
	}

	@DeleteMapping("/{id}")
	public void evictBook(@PathVariable Long id) {
		this.bookService.removeBookFromCache(id);
	}
}
