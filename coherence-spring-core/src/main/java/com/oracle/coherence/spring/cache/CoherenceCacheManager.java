/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

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

	private final Coherence coherence;
	private final CoherenceCacheConfiguration defaultCacheConfiguration;

	private final Map<String, CoherenceCache> coherenceCacheMap = new ConcurrentHashMap<String, CoherenceCache>(16);

	/**
	 * Constructs a new {@link CoherenceCacheManager} using the provided {@link Coherence} instance. The underlying
	 * {@link CoherenceCacheConfiguration} will be initialized with a timeToLive value of {@link Duration#ZERO}, which
	 * means that the expiration value for cache values will NOT be specified when performing cache puts. However, depending
	 * on your Coherence cache configuration in {@code coherence-cache-config.xml} cache values may still expire.
	 * @param coherence must not be null
	 */
	public CoherenceCacheManager(Coherence coherence) {
		this(coherence, new CoherenceCacheConfiguration(Duration.ZERO));
	}

	/**
	 * Constructs a new {@link CoherenceCacheManager} using the provided {@link Coherence} instance.
	 * @param coherence must not be null
	 * @param defaultCacheConfiguration must not be null
	 */
	public CoherenceCacheManager(Coherence coherence, CoherenceCacheConfiguration defaultCacheConfiguration) {
		Assert.notNull(coherence, "The coherence instance must not be null.");
		Assert.notNull(coherence, "defaultCacheConfiguration must not be null.");
		this.coherence = coherence;
		this.defaultCacheConfiguration = defaultCacheConfiguration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cache getCache(String name) {
		final CoherenceCache cache = this.coherenceCacheMap.get(name);

		if (cache == null) {
			final Session session = this.coherence.getSession();
			final String cacheNameToUse = this.defaultCacheConfiguration.getCacheName(name);
			final NamedCache<Object, Object> namedCache = session.getCache(cacheNameToUse);
			final CoherenceCache coherenceCache = new CoherenceCache(namedCache, this.defaultCacheConfiguration);
			final CoherenceCache preExistingCoherenceCache = this.coherenceCacheMap.putIfAbsent(name, coherenceCache);

			return (preExistingCoherenceCache != null) ? preExistingCoherenceCache : coherenceCache;
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
