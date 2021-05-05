/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.model.repositories;

import java.util.Collection;
import java.util.List;

import com.oracle.coherence.spring.data.model.Author;
import com.oracle.coherence.spring.data.model.Book;
import com.tangosol.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;


public interface BookRepository extends CrudRepository<Book, UUID> {

	boolean existsByAuthor(Author author);

	List<Book> findByAuthor(Author author);

	List<Book> findByTitleIgnoreCase(String title);

	List<Book> findByPagesGreaterThanEqual(int pages);

	List<Book> findByPagesLessThanEqual(int pageCount);

	List<Book> findByTitleLike(String like);

	List<Book> findByTitleNotLike(String like);

	List<Book> findByTitleLikeIgnoreCase(String like);

	List<Book> findByTitleNotLikeIgnoreCase(String like);

	List<Book> findByPagesGreaterThan(int pageCount);

	List<Book> findByPagesLessThan(int pageCount);

	List<Book> findByPublicationYearAfter(int year);

	List<Book> findByPublicationYearBefore(int year);

	List<Book> findByTitleContains(String keyword);

	List<Book> findByTitleNotContains(String keyword);

	List<Book> findByTitleStartingWith(String keyword);

	List<Book> findByTitleStartingWithIgnoreCase(String keyword);

	List<Book> findByTitleEndingWith(String keyword);

	List<Book> findByTitleEndingWithIgnoreCase(String keyword);

	List<Book> findByTitleIn(Collection<String> titles);

	List<Book> findByTitleNotIn(Collection<String> titles);

	List<Book> findByPublicationYearBetween(int startYear, int endYear);

	List<Book> findByAuthorIsNull();

	List<Book> findByAuthorIsNotNull();

	List<Book> findByAuthorOrderByTitleAsc(Author author);

	List<Book> findByAuthorOrderByTitleDesc(Author author);

	List<Book> findTop2ByPagesGreaterThanOrderByTitleAsc(int pageCount);

	List<Book> findTop2ByPagesGreaterThanOrderByTitleDesc(int pageCount);

	List<Book> findTop3ByPagesGreaterThan(int pageCount, Sort sort);

	List<Book> findByTitleMatches(String regex);

	List<Book> findByLongBookIsTrue();

	List<Book> findByLongBookIsFalse();

	List<Book> findByAuthorNot(Author author);

	List<Book> findByChaptersEmpty();

	List<Book> findByChaptersNotEmpty();
	Streamable<Book> streamByAuthor(Author author);

	int deleteByTitleStartingWith(String title);

	Page<Book> findByAuthor(Author author, Pageable pageable);

	Slice<Book> findByTitle(String title, Pageable pageable);
}
