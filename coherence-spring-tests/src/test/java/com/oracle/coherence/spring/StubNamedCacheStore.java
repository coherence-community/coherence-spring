package com.oracle.coherence.spring;

import com.tangosol.net.cache.MapCacheStore;

import java.util.Map;

/**
 * Stub CacheStore implementation for testing by {@link SpringNamespaceHandlerTests}.
 *
 * @author pp  2012.02.08
 */
public class StubNamedCacheStore
        extends MapCacheStore
    {
    /**
     * Create a CacheStore that delegates to a Map.
     *
     * @param map the Map to use as the underlying store for this CacheStore
     */
    public StubNamedCacheStore(Map map)
        {
        super(map);
        }

    public String getCacheName()
        {
        return m_sCacheName;
        }

    public void setCacheName(String sCacheName)
        {
        m_sCacheName = sCacheName;
        }

    public String getSpelValue()
        {
        return m_sSpelValue;
        }

    public void setSpelValue(String sValue)
        {
        m_sSpelValue = sValue;
        }

    @Override
    public Object load(Object oKey)
        {
        if (CACHE_NAME_KEY.equals(oKey))
            {
            return getCacheName();
            }
        return super.load(oKey);
        }

    private String m_sCacheName;
    private String m_sSpelValue;

    public static final String CACHE_NAME_KEY = "CACHE_NAME_KEY";
    }
