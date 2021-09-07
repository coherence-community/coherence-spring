/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cachestore;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.tangosol.net.cache.CacheStore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * A generic Spring JPA {@link CacheStore} that extends a Spring {@link JpaRepository} for
 * the database operations.
 *
 * @param <T> the domain type the repository manages and the type of values in the cache
 * @param <ID> the type of the id of the entity the repository manages and the type of the
 * cache key
 * @author Jonathan Knight 2021.08.17
 * @since 3.0
 */
@NoRepositoryBean
public interface JpaRepositoryCacheStore<T, ID> extends JpaRepositoryCacheLoader<T, ID>, CacheStore<ID, T> {

	@Transactional
	@Override
	default void erase(ID key) {
		deleteById(key);
		flush();
	}

	@Transactional
	@Override
	default void eraseAll(Collection<? extends ID> colKeys) {
		boolean fRemove = true;
		for (Iterator<? extends ID> iter = colKeys.iterator(); iter.hasNext();) {
			this.deleteById(iter.next());
			erase(iter.next());
			if (fRemove) {
				try {
					iter.remove();
				}
				catch (UnsupportedOperationException ex) {
					fRemove = false;
				}
			}
		}
		flush();
	}

	@Transactional
	@Override
	default void store(ID key, T value) {
		saveAndFlush(value);
	}

	@Transactional
	@Override
	default void storeAll(Map<? extends ID, ? extends T> entries) {
		boolean fRemove = true;
		for (Iterator<? extends Map.Entry<? extends ID, ? extends T>> iter = entries.entrySet().iterator(); iter
				.hasNext();) {
			Map.Entry<? extends ID, ? extends T> entry = iter.next();
			save(entry.getValue());
			if (fRemove) {
				try {
					iter.remove();
				}
				catch (UnsupportedOperationException ex) {
					fRemove = false;
				}
			}
			flush();
		}
	}

}
