/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example.model;

import com.oracle.coherence.spring.cachestore.JpaRepositoryCacheStore;
import org.springframework.stereotype.Repository;

/**
 * A JPA repository cache store for the {@link Person} entity.
 *
 * <b>Note:</b> The bean must be given a name
 *
 * @author Jonathan Knight 2021.08.17
 */
@Repository
public interface PersonRepository extends JpaRepositoryCacheStore<Person, Long> {

}
