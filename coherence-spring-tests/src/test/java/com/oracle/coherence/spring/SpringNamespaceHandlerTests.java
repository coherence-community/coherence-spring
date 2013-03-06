package com.oracle.coherence.spring;

import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.AbstractCacheLoader;
import com.tangosol.net.cache.LocalCache;

import com.tangosol.util.ExternalizableHelper;

import common.AbstractFunctionalTest;

import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.beans.factory.BeanFactory;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link com.tangosol.coherence.spring.SpringNamespaceHandler}.
 *
 * @author pp  2012.01.31
 */
public class SpringNamespaceHandlerTests
        extends AbstractFunctionalTest
    {
    public SpringNamespaceHandlerTests()
        {
        super("spring-cache-config.xml");
        }

    /**
     * Initialize the test class.
     */
    @BeforeClass
    public static void _startup()
        {
        // make sure we use ECCF
        System.setProperty("tangosol.coherence.cachefactory",
                "com.tangosol.net.ExtensibleConfigurableCacheFactory");

        // this test requires local storage to be enabled
        System.setProperty("tangosol.coherence.distributed.localstorage", "true");

        AbstractFunctionalTest._startup();
        }

    /**
     * Test the use of Spring to inject a CacheStore.
     */
    @Test
    public void testCacheStore()
        {
        String[] asCacheNames = new String[] {"CacheStore", "CacheStorePull"};
        ConfigurableCacheFactory laccf = (ConfigurableCacheFactory) getFactory();

        for (String sCacheName : asCacheNames)
            {
            NamedCache cache = laccf.ensureCache(sCacheName, null);

            // the CacheStore provided by Spring is an instance of MapCacheStore
            // which has an internal map that contains the entry <"key", "value">
            assertEquals("value", cache.get("key"));

            // this asserts that the {cache-name} macro succeeded in injecting
            // the cache name to the cache store (see StubNamedCacheStore)
            assertEquals(sCacheName, cache.get(StubNamedCacheStore.CACHE_NAME_KEY));
            }

        BeanFactory         beanFactory = laccf.getResourceRegistry().getResource(BeanFactory.class, "default");
        StubNamedCacheStore cs         = beanFactory.getBean("mapCacheStorePull", StubNamedCacheStore.class);
        assertThat(cs.getSpelValue(), is("Prosper"));
        }

    /**
     * Test the injection of a backing map manager context via
     * the {manager-context} macro.
     */
    @Test
    public void testBackingMapManagerContextInjection()
        {
        String[] asCacheNames = new String[] {"CacheBML", "CacheBMLPull"};
        String[] asBeanNames  = new String[] {"bml", "bmlPull"};
        ConfigurableCacheFactory laccf = (ConfigurableCacheFactory) getFactory();


        for (int i = 0; i < asCacheNames.length; ++i)
            {
            NamedCache             cache       = laccf.ensureCache(asCacheNames[i], null);
            BeanFactory            beanFactory = laccf.getResourceRegistry().getResource(BeanFactory.class, "default");
            StubBackingMapListener bml         = beanFactory.getBean(asBeanNames[i], StubBackingMapListener.class);

            assertFalse(bml.isContextConfigured());

            cache.put("key", "value");

            assertTrue(bml.isContextConfigured());
            }
        }

    /**
     * Test the registration of a bean factory and injection of a backing map.
     */
    @Test
    public void testManualRegistration()
        {
        // this local cache will be used as a backing map
        LocalCache localCache = new LocalCache(100, 0, new AbstractCacheLoader()
            {
            @Override
            public Object load(Object oKey)
                {
                return ExternalizableHelper.toBinary("mock");
                }
            }
        );

        // instead of creating a Spring application context, create
        // a simple mock BeanFactory that returns the local cache
        // created above
        BeanFactory factory = mock(BeanFactory.class);
        when(factory.getBean("localBackingMap")).thenReturn(localCache);

        ConfigurableCacheFactory ccf = getFactory();

        // register the mock BeanFactory with the cache factory so that
        // it is used as the backing map (see the cache config file)
        ccf.getResourceRegistry().registerResource(BeanFactory.class, "mock", factory);

        NamedCache namedCache = ccf.ensureCache("CacheCustomBackingMap", null);

        // cache loader always returns the same value
        assertEquals("mock", namedCache.get("key"));

        // assert backing map properties
        Map mapBacking = namedCache.getCacheService().getBackingMapManager().getContext().
                getBackingMapContext("CacheCustomBackingMap").getBackingMap();

        assertEquals(LocalCache.class, mapBacking.getClass());
        assertEquals(100, ((LocalCache) mapBacking).getHighUnits());
        assertEquals(localCache, mapBacking);
        }

    /**
     * Test interceptor configuration.
     */
    @Test
    public void testInterceptor()
        {
        assertFalse(StubInterceptor.eventReceived());

        getFactory().ensureCache("CacheInterceptor", null).put("key", "value");

        assertTrue(StubInterceptor.eventReceived());
        }
    }