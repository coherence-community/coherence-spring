/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
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
