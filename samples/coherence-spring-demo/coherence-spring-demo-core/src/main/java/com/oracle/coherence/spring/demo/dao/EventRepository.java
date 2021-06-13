/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oracle.coherence.spring.demo.model.Event;

/**
 *
 * @author Gunnar Hillert
 *
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
