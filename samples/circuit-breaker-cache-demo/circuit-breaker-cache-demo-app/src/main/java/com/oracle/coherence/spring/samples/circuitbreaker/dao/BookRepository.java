/*
 * Copyright (c) 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker.dao;

import com.oracle.coherence.spring.samples.circuitbreaker.model.Book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for retrieving and persisting {@link Book}.
 * @author Gunnar Hillert
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
