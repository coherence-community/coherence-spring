/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;

/**
 *
 * Implementation of the {@link CacheManager} for Oracle Coherence.
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceCacheManager implements CacheManager {

	private Coherence coherence;

	private final Map<String, CoherenceCache> coherenceCacheMap = new ConcurrentHashMap<String, CoherenceCache>(16);

	public CoherenceCacheManager(Coherence coherence) {
		super();
		this.coherence = coherence;
	}

	/**
	 *
	 */
	@Override
	public Cache getCache(String name) {
		final CoherenceCache cache = coherenceCacheMap.get(name);

		if (cache == null) {
			final NamedCache<Object, Object> namedCache = this.coherence.getSession().getCache(name);
			final CoherenceCache coherenceCache = new CoherenceCache(namedCache);
			final CoherenceCache preExitingCoherenceCache = this.coherenceCacheMap.putIfAbsent(name, coherenceCache);

			return preExitingCoherenceCache != null ? preExitingCoherenceCache : coherenceCache;
		}
		else {
			return cache;
		}
	}

	/**
	 *
	 */
	@Override
	public Collection<String> getCacheNames() {
		return this.coherenceCacheMap.keySet();
	}

}
