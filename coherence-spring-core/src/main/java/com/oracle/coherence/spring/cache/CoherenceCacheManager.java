/*
 * File: CoherenceCacheManager.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
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
