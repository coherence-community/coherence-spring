/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker.service.impl;

import java.util.List;

import com.oracle.coherence.spring.samples.circuitbreaker.dao.BookRepository;
import com.oracle.coherence.spring.samples.circuitbreaker.model.Book;
import com.oracle.coherence.spring.samples.circuitbreaker.service.BookService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* Deault implementation of the {@link BookService}.
* @author Gunnar Hillert
*/
@Transactional
@Service
public class DefaultBookService implements BookService {

	private static final Logger logger = LoggerFactory.getLogger(DefaultBookService.class);

	@Autowired
	private BookRepository bookRepository;

	/**
	 * Retrieve all books. If available, books will be returned from the Coherence cache. If the Coherence*Extend
	 * connection is not available or timing out, the Resilience4j Circuit Breaker is opened and the fallback method
	 * {@link #getAllBooksFallback(Throwable)} is invoked, which will return all books from the pesistence store.
	 * @return all books
	 */
	@Cacheable("allBooks")
	@CircuitBreaker(name = "coherence", fallbackMethod = "getAllBooksFallback")
	@Override
	public List<Book> getAllBooks() {
		if (logger.isInfoEnabled()) {
			logger.info("Retrieving all books...");
		}

		final List<Book> books = this.bookRepository.findAll();

		if (logger.isInfoEnabled()) {
			logger.info("Retrieved {} book(s).", books.size());
		}
		return books;
	}

	/**
	 * Resilience4j fallback method for {@link #getBook(Long)}. Will catch any exception.
	 * @param throwable the exception that caused this fallback method to be called
	 * @return a list of all books
	 */
	public List<Book> getAllBooksFallback(Throwable throwable) {
		if (logger.isWarnEnabled()) {
			logger.warn("Executing fallback method due to exception: " + throwable.getClass().getName());
		}
		else if (logger.isDebugEnabled()) {
			logger.warn("Executing fallback method due to exception:", throwable);
		}
		return this.bookRepository.findAll();
	}

	@CachePut(cacheNames = "books", key = "#result.id")
	@Override
	public Book addBook(Book book) {
		if (logger.isInfoEnabled()) {
			logger.info("Add a new book - {}", book.getTitle());
		}
		final Book savedBook = this.bookRepository.save(book);
		return savedBook;
	}

	/**
	 * Get a books for the provided {@link Book} id. If you call this method with an id of {@code 42}, a simulated
	 * {@link BusinessException} will be thrown.
	 * @param id the id of the book
	 * @return the book for with the provided id
	 */
	@Cacheable(cacheNames = "books", key = "#id")
	@CircuitBreaker(name = "coherence", fallbackMethod = "getBookFallback")
	@Override
	public Book getBook(Long id) {
		if (id == 42) {
			if (logger.isInfoEnabled()) {
				logger.info("Going to throw a fake BusinessException for the book with id '{}'.", id);
			}
			throw new BusinessException("Something expected happened.");
		}

		if (logger.isInfoEnabled()) {
			logger.info("Going to retrieve the Book with id '{}' from the persistence store.", id);
		}
		return this.bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No book found for id: " + id));
	}

	/**
	 * Resilience4j selects fallback methods based on the specificity of the thrown Exceptions. It is important to know
	 * that expected exceptions (e.g. Business Exceptions) that you included in the list of {@code ignoreExceptions} will
	 * also trigger the execution of fallBack methods.
	 * @param id the Book Id
	 * @param ex the BusinessException
	 * @return always re-throws the BusinessException
	 * @throws BusinessException re-throws the passed in exception
	 */
	public Book getBookFallback(Long id, BusinessException ex) {
		if (logger.isWarnEnabled()) {
			logger.warn("getBook fallback method (For BusinessException) was called with message: " + ex.getMessage());
		}
		throw ex;
	}

	/**
	 * Resilience4j fallback method for {@link #getBook(Long)}. The least specific fallback method. It is basically a
	 * catch-all for exceptions not handled for more specific fallback methods.
	 * @param id the id of the book
	 * @param ex the exception that caused the fallback method to be called
	 * @return the book from the persistence store
	 */
	public Book getBookFallback(Long id, Throwable ex) {
		if (logger.isWarnEnabled()) {
			logger.warn("getBook fallback method (For Throwable) was called: " + ex.getMessage());
		}
		else if (logger.isDebugEnabled()) {
			logger.debug("getBook fallback method (For Throwable) was called:", ex);
		}
		return this.bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No book found for id: " + id));
	}

	@Override
	@CacheEvict(cacheNames = "books")
	public void removeBookFromCache(Long id) {
		if (logger.isInfoEnabled()) {
			logger.info("No-op method that will evict the book with id '{}' from the Coherence cache.", id);
		}
	}

}
