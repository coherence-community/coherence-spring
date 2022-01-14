/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker;

import java.time.LocalDate;

import com.oracle.coherence.spring.samples.circuitbreaker.model.Book;
import com.oracle.coherence.spring.samples.circuitbreaker.service.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry to the application.
 * @author Gunnar Hillert
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class CircuitBreakerApplication implements ApplicationRunner {

	@Autowired
	private BookService eventService;

	public static void main(String[] args) {
		SpringApplication.run(CircuitBreakerApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		final Book book1 = new Book("Title", 122, "author", LocalDate.now());
		final Book book2 = new Book("Title2", 122, "author2", LocalDate.now());
		this.eventService.addBook(book1);
		this.eventService.addBook(book2);
	}
}
