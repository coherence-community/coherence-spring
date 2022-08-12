/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.hibernate.dao;

import java.util.List;

import jakarta.persistence.QueryHint;

import com.oracle.coherence.spring.samples.hibernate.model.Plant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA Repository responsible for returning {@link Plant}s.
 * @author Gunnar Hillert
 *
 */
@Repository
@Transactional
public interface PlantRepository extends JpaRepository<Plant, Long> {

	/**
	 * Return all persisted {@link Plant}s. Uses {@link QueryHint} to enable Hibernate Second Level Caching for the
	 * query.
	 * @return all persisted plants
	 */
	@QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
	@Query("select p from Plant p")
	List<Plant> getAllPlantsCacheable();

}
