/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.model.repositories;

import java.util.List;

import com.oracle.coherence.spring.data.config.CoherenceMap;
import com.oracle.coherence.spring.data.model.Author;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.repository.CoherenceRepository;
import com.tangosol.util.UUID;

@CoherenceMap("book")
public interface CoherenceBookRepository extends CoherenceRepository<Book, UUID> {

	List<Book> findByAuthor(Author author);
}
