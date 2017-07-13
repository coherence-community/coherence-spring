/*
 * File: SpringApplicationTests.java
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

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.net.cache.AbstractCacheLoader;
import com.tangosol.net.cache.LocalCache;

import com.tangosol.util.ExternalizableHelper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.springframework.beans.factory.BeanFactory;

import org.springframework.context.ApplicationContext;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

/**
 * Tests for {@link SpringNamespaceHandler}.
 *
 * @author Brian Oliver
 */
public class SpringApplicationTests
{
    /**
     * The {@link ApplicationContext} we'll use for testing.
     */
    protected static ApplicationContext context;

    /**
     * The {@link SpringBasedCoherenceSession} we'll use for testing.
     */
    protected static SpringBasedCoherenceSession session;


    /**
     * Create an {@link ApplicationContext} and {@link SpringBasedCoherenceSession}
     * for our tests (we share these)
     */
    @BeforeClass
    public static void startCluster()
    {
        context = new AnnotationConfigApplicationContext(SpringApplicationConfig.class);

        session = context.getBean(SpringBasedCoherenceSession.class);
    }


    /**
     * Stop the cluster after each test have executed.
     */
    @AfterClass
    public static void stopCluster()
    {
        CacheFactory.shutdown();
    }


    /**
     * Test the use of Spring to inject a CacheStore.
     */
    @Test
    public void testCacheStore()
    {
        String[] asCacheNames = new String[] {"CacheStore", "CacheStorePull"};

        for (String sCacheName : asCacheNames)
        {
            NamedCache cache = session.getCache(sCacheName);

            // the CacheStore provided by Spring is an instance of MapCacheStore
            // which has an internal map that contains the entry <"key", "value">
            assertEquals("value", cache.get("key"));

            // this asserts that the {cache-name} macro succeeded in injecting
            // the cache name to the cache store (see StubNamedCacheStore)
            assertEquals(sCacheName, cache.get(StubNamedCacheStore.CACHE_NAME_KEY));
        }

        BeanFactory         beanFactory = session.getResourceRegistry().getResource(BeanFactory.class);
        StubNamedCacheStore cs          = beanFactory.getBean("mapCacheStorePull", StubNamedCacheStore.class);

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

        for (int i = 0; i < asCacheNames.length; ++i)
        {
            NamedCache             cache       = session.getCache(asCacheNames[i]);
            BeanFactory            beanFactory = session.getResourceRegistry().getResource(BeanFactory.class);
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
        });

        // instead of creating a Spring application context, create
        // a simple mock BeanFactory that returns the local cache
        // created above
        BeanFactory factory = mock(BeanFactory.class);

        when(factory.getBean("localBackingMap")).thenReturn(localCache);

        // register the mock BeanFactory with the cache factory so that
        // it is used as the backing map (see the cache config file)
        session.getResourceRegistry().registerResource(BeanFactory.class, "mock", factory);

        NamedCache namedCache = session.getCache("CacheCustomBackingMap");

        // cache loader always returns the same value
        assertEquals("mock", namedCache.get("key"));

        // assert backing map properties
        Map mapBacking =
            namedCache.getCacheService().getBackingMapManager().getContext()
                .getBackingMapContext("CacheCustomBackingMap").getBackingMap();

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
        StubInterceptor interceptor = context.getBean(StubInterceptor.class);

        assertFalse(interceptor.eventReceived());
        session.getCache("CacheInterceptor").put("key", "value");
        assertTrue(interceptor.eventReceived());
    }
}
