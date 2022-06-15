/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.model.repositories;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import com.oracle.coherence.spring.data.config.CoherenceMap;
import com.oracle.coherence.spring.data.model.Author;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.BookProjection;
import com.oracle.coherence.spring.data.model.NestedBookProjection;
import com.oracle.coherence.spring.data.model.NestedOpenBookProjection;
import com.oracle.coherence.spring.data.model.OpenBookProjection;
import com.oracle.coherence.spring.data.model.PublicationYearClassProjection;
import com.tangosol.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Vaso Putica
 * @author Gunnar Hillert
 */
@CoherenceMap("book")
public interface BookProjectionRepository extends CrudRepository<Book, UUID> {

	List<BookProjection> findByPages(int pages);

	List<OpenBookProjection> findByTitle(String title);

	List<NestedBookProjection> findByTitleContains(String keyword);

	List<NestedOpenBookProjection> findByTitleEndingWith(String keyword);

	List<PublicationYearClassProjection> findByPublished(LocalDate date);

	<T> Collection<T> findByTitle(String title, Class<T> type);

	List<OpenBookProjection> findByTitleStartingWithOrderByPagesDesc(String keyword);

	List<NestedBookProjection> findByAuthor(Author author, Sort sort);

	List<BookProjection> findByAuthorOrderByPagesAsc(Author author);

	List<NestedBookProjection> findByAuthorOrderByPagesDesc(Author author);

	List<BookProjection> findBy(Sort sort);

	List<BookProjection> findByOrderByPagesAsc();
}
