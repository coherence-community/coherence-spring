/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cachestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import com.oracle.coherence.spring.CoherenceContext;
import com.tangosol.net.cache.CacheLoader;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * A Coherence {@link CacheLoader} that is also a {@link JpaRepository} allowing the
 * loader methods to use the repository to load data.
 *
 * @param <T> the domain type the repository manages and the type of values in the cache
 * @param <ID> the type of the id of the entity the repository manages and the type of the
 * cache key
 * @author Jonathan Knight 2021.08.17
 * @since 3.0
 */
@NoRepositoryBean
public interface JpaRepositoryCacheLoader<T, ID> extends JpaRepository<T, ID>, CacheLoader<ID, T> {

	/**
	 * Returns the cache key for the given entity value.
	 * @param value the entity value to obtain the cache key from
	 * @return the cache key for the given entity value
	 */
	@SuppressWarnings("unchecked")
	default ID getId(T value) {
		EntityManager em = CoherenceContext.getApplicationContext().getBean(EntityManager.class);
		return (ID) em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(value);
	}

	@Transactional
	@Override
	default T load(ID key) {
		return findById(key).orElse(null);
	}

	@Transactional
	@Override
	@SuppressWarnings("unchecked")
	default Map<ID, T> loadAll(Collection<? extends ID> colKeys) {
		Map<ID, T> map = new HashMap<>();
		Collection<ID> keys = (Collection<ID>) colKeys;
		findAllById(keys).forEach((v) -> map.put(getId(v), v));
		return map;
	}

}
