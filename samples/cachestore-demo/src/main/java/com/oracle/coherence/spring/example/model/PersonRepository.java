/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example.model;

// # tag::imports[]
import com.oracle.coherence.spring.cachestore.JpaRepositoryCacheStore;
import org.springframework.stereotype.Repository;
// # end::imports[]

/**
 * A JPA repository cache store for the {@link Person} entity.
 *
 * <b>Note:</b> The bean must be given a name
 *
 * @author Jonathan Knight 2021.08.17
 */
// # tag::personRepo[]
@Repository
public interface PersonRepository extends JpaRepositoryCacheStore<Person, Long> {

}
// # end::personRepo[]
