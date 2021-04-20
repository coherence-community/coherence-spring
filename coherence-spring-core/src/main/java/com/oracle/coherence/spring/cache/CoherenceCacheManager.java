/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.util.Assert;

/**
 *
 * Implementation of the {@link CacheManager} for Oracle Coherence.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceCacheManager implements CacheManager {

	private Coherence coherence;

	private final Map<String, CoherenceCache> coherenceCacheMap = new ConcurrentHashMap<String, CoherenceCache>(16);

	/**
	 * Constructs a new {@link CoherenceCacheManager} using the provided {@link Coherence} instance.
	 * @param coherence must not be null
	 */
	public CoherenceCacheManager(Coherence coherence) {
		Assert.notNull(coherence, "The coherence instance must not be null.");
		this.coherence = coherence;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cache getCache(String name) {
		final CoherenceCache cache = this.coherenceCacheMap.get(name);

		if (cache == null) {
			final NamedCache<Object, Object> namedCache = this.coherence.getSession().getCache(name);
			final CoherenceCache coherenceCache = new CoherenceCache(namedCache);
			final CoherenceCache preExitingCoherenceCache = this.coherenceCacheMap.putIfAbsent(name, coherenceCache);

			return (preExitingCoherenceCache != null) ? preExitingCoherenceCache : coherenceCache;
		}
		else {
			return cache;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<String> getCacheNames() {
		return this.coherenceCacheMap.keySet();
	}

}
