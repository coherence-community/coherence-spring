/*
 * File: SpringBasedCoherenceSession.java
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

import com.tangosol.coherence.config.ParameterMacroExpressionParser;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.ExtensibleConfigurableCacheFactory;
import com.tangosol.net.NamedCache;

import com.tangosol.util.ResourceRegistry;

import org.springframework.beans.BeansException;

import org.springframework.beans.factory.BeanFactory;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;

import static com.tangosol.net.ExtensibleConfigurableCacheFactory.DependenciesHelper.newInstance;

/**
 * Provides the ability to acquire Coherence-based resources in the
 * Spring Framework.
 * <p>
 * Spring-based Applications will typically use this to access resources
 * (NamedCaches) provided by Coherence.   Using this class allows Spring
 * to both inject values into Coherence and Coherence to request Beans from
 * Spring, all without requiring the use of static member variables or methods.
 * <p>
 * Typically developers will instantiate an instance of a session to share
 * across their application from with in their application configuration class.
 * ie: the class(es) annotated with @Configuration
 * <p>
 * For Example:
 * <code>
 * @Configuration
 * public class ApplicationConfiguration
 * {
 *     @Bean
 *     public SpringBasedCoherenceSession session()
 *     {
 *         return new SpringBasedCoherenceSession("cache-config-file.xml);
 *     }
 * }
 * </code>
 *
 * To acquire resources (like NamedCaches) from a session, they can be similarly
 * resolved (and injected).
 *
 * <code>
 * @Configuration
 * public class ApplicationConfiguration
 * {
 *     @Bean
 *     public SpringBasedCoherenceSession session()
 *     {
 *         return new SpringBasedCoherenceSession("cache-config-file.xml);
 *     }
 *
 *     @Bean
 *     public NamedCache accounts()
 *     {
 *         return session.getCache("accounts");
 *     }
 * }
 * </code>
 *
 * Copyright (c) 2015-2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SpringBasedCoherenceSession implements ApplicationContextAware,
                                                    ApplicationListener<ApplicationContextEvent>
{
    /**
     * The default URI constant when we need to auto-detect the cache configuration
     * (or use system properties).
     */
    private static final String DEFAULT_CACHE_CONFIG_URI = "$Default$";

    /**
     * The {@link ApplicationContext} in which this {@link SpringBasedCoherenceSession}
     * is operating.
     */
    private ApplicationContext applicationContext;

    /**
     * The {@link ExtensibleConfigurableCacheFactory} the {@link SpringBasedCoherenceSession}
     * will use for acquiring resources from Coherence.
     */
    private ExtensibleConfigurableCacheFactory configurableCacheFactory;

    /**
     * The URI of the Coherence Cache Configuration File.
     */
    private String cacheConfigURI;


    /**
     * Constructs a {@link SpringBasedCoherenceSession} that will
     * auto-detect the cache configuration.
     */
    public SpringBasedCoherenceSession()
    {
        this(DEFAULT_CACHE_CONFIG_URI);
    }


    /**
     * Constructs a {@link SpringBasedCoherenceSession} using the specified
     * cache configuration.
     *
     * @param cacheConfigURI  the URI of the cache configuration
     */
    public SpringBasedCoherenceSession(String cacheConfigURI)
    {
        this.applicationContext       = null;
        this.configurableCacheFactory = null;
        this.cacheConfigURI           = cacheConfigURI;
    }


    /**
     * Acquire the specified {@link NamedCache}.
     *
     * @param name  the name of the cache
     *
     * @return a {@link NamedCache}
     */
    public NamedCache getCache(String name)
    {
        return configurableCacheFactory.ensureCache(name, null);
    }


    /**
     * Obtain the {@link ResourceRegistry} for the {@link SpringBasedCoherenceSession}.
     *
     * @return the {@link ResourceRegistry}
     */
    public ResourceRegistry getResourceRegistry()
    {
        return configurableCacheFactory.getResourceRegistry();
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;

        // override the bean expression resolver with one that can also
        // resolve Coherence-based values eg: cache-name, manager-context etc.
        AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();

        if (beanFactory instanceof ConfigurableBeanFactory)
        {
            ((ConfigurableBeanFactory) beanFactory)
                .setBeanExpressionResolver(new CoherenceBeanExpressionResolver(ParameterMacroExpressionParser
                    .INSTANCE));
        }

        // establish the configuration dependencies for the
        // ConfigurableCacheFactory we're about to create
        ExtensibleConfigurableCacheFactory.Dependencies dependencies = cacheConfigURI.equals(DEFAULT_CACHE_CONFIG_URI)
                                                                       ? newInstance() : newInstance(cacheConfigURI);

        // register this application context as a resource so that the
        // ConfigurableCacheFactory can later use it
        // (say for injecting into Coherence-based instances)
        dependencies.getResourceRegistry().registerResource(ApplicationContext.class, applicationContext);

        // also register this application context as the default BeanFactory
        dependencies.getResourceRegistry().registerResource(BeanFactory.class, applicationContext);

        // create the ConfigurableCacheFactory
        this.configurableCacheFactory = new ExtensibleConfigurableCacheFactory(dependencies);

        // register the ConfigurableCacheFactory with the CacheFactoryBuilder
        // so that anyone attempting to use the static CacheFactory methods
        // like "CacheFactory.getCache()" has a chance of using the same
        // configuration and caches!
        CacheFactory.getCacheFactoryBuilder().setConfigurableCacheFactory(configurableCacheFactory,
                                                                          cacheConfigURI,
                                                                          null,
                                                                          false);
    }


    @Override
    public void onApplicationEvent(ApplicationContextEvent event)
    {
        if (event instanceof ContextRefreshedEvent)
        {
            // we don't re-cycle / restart the ConfigurableCacheFactory
            // when the application is refreshed
        }
        else if (event instanceof ContextStartedEvent)
        {
            configurableCacheFactory.activate();
        }
        else if (event instanceof ContextStoppedEvent)
        {
            configurableCacheFactory.dispose();

            CacheFactory.getCacheFactoryBuilder().release(configurableCacheFactory);

            configurableCacheFactory = null;
        }
    }
}
