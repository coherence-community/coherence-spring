/*
 * File: CoherenceCache.java
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

import java.util.Collections;
import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import com.tangosol.net.NamedCache;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceCache implements Cache {

	private final NamedCache<Object, Object> cache;

	public CoherenceCache(NamedCache<Object, Object> cache) {
		super();
		this.cache = cache;
	}

	@Override
	public void clear() {
		this.cache.clear();
	}

	@Override
	public void evict(Object key) {
		this.cache.remove(key);
	}

	@Override
	public ValueWrapper get(Object key) {
		final Object value = this.cache.get(key);
		return value != null ? new SimpleValueWrapper(value) : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Class<T> type) {
		final Object value = this.cache.get(key);
		if (type != null && value != null && !type.isInstance(value)) {
			throw new IllegalStateException(
				String.format("Cached value '%s' is not of required type '%s'.", value, type.getName()));
		}
		return (T) value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Callable<T> valueLoader) {
		final Object value = this.cache.get(key);
		if (value != null) {
			return (T) value;
		} else {
			this.cache.lock(key);
			try {
				final Object value2 = this.cache.get(key);
				if (value2 != null) {
					return (T) value2;
				} else {
					return loadValue(key, valueLoader);
				}
			} finally {
				this.cache.unlock(key);
			}
		}
	}

	@Override
	public String getName() {
		return this.cache.getCacheName();
	}

	@Override
	public Object getNativeCache() {
		return this.cache;
	}

	private <T> T loadValue(Object key, Callable<T> valueLoader) {
		T value;
		try {
			value = valueLoader.call();
		} catch (Exception e) {
			throw new IllegalStateException("Executing the callabe failed.", e);
		}
		put(key, value);
		return value;
	}

	@Override
	public void put(Object key, Object value) {
		if (value == null) {
			return;
		}
		this.cache.putAll(Collections.singletonMap(key, value));
	}

	/**
	 * Returns the number of key-value mappings of the underlying {@link NamedCache}.
	 *
	 * @return the number of key-value mappings in this map
	 */
	public int size() {
		return this.cache.size();
	}
}
