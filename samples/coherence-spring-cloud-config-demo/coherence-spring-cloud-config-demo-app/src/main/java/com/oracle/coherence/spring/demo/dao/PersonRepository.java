/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oracle.coherence.spring.demo.model.Person;

/**
 *
 * @author Gunnar Hillert
 *
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

}
