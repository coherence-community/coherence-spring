/*
 * File: StubNamedCacheStore.java
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

package com.oracle.coherence.spring;

import com.tangosol.net.cache.MapCacheStore;

import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Stub CacheStore implementation for testing by {@link SpringNamespaceHandlerTests}.
 *
 * @author Patrick Peralta
 * @author Brian Oliver
 */
public class StubNamedCacheStore extends MapCacheStore
{
	/**
	 * Cache name constant.
	 */
	public static final String CACHE_NAME_KEY = "CACHE_NAME_KEY";

	private String             m_sCacheName;
	private String             m_sSpelValue;


	/**
	 * Create a CacheStore that delegates to a Map.
	 *
	 * @param map the Map to use as the underlying store for this CacheStore
	 */
	public StubNamedCacheStore(Map map)
	{
		super(map);
	}


	/**
	 * Return the cache name for this cache store.
	 *
	 * @return cache name
	 */
	public String getCacheName()
	{
		return m_sCacheName;
	}


	/**
	 * Set the cache name for this cache store.
	 *
	 * @param sCacheName cache name
	 */
	@Value("#{cache-name}")
	public void setCacheName(String sCacheName)
	{
		m_sCacheName = sCacheName;
	}


	/**
	 * Return the spel value for this cache store.
	 *
	 * @return spel value
	 */
	public String getSpelValue()
	{
		return m_sSpelValue;
	}


	/**
	 * Set the spel value for this cache store.
	 *
	 * @param sValue spel value
	 */
	@Value("#{'Prosperity'.substring(0,7)}")
	public void setSpelValue(String sValue)
	{
		m_sSpelValue = sValue;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object load(Object oKey)
	{
		if (CACHE_NAME_KEY.equals(oKey))
		{
			return getCacheName();
		}

		return super.load(oKey);
	}
}
