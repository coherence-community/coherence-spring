/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import com.tangosol.coherence.config.ParameterMacroExpressionParser;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ExtensibleConfigurableCacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Service;
import com.tangosol.util.ResourceRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;

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
 * <pre>
 * <code>
 * {@literal @Configuration}
 * public class ApplicationConfiguration
 * {
 *     {@literal @Bean}
 *     public SpringBasedCoherenceSession session()
 *     {
 *         return new SpringBasedCoherenceSession("cache-config-file.xml);
 *     }
 * }
 * </code></pre>
 *
 * To acquire resources (like NamedCaches) from a session, they can be similarly
 * resolved (and injected).
 *
 * <pre>
 * <code>
 * {@literal @Configuration}
 * public class ApplicationConfiguration
 * {
 *     {@literal @Bean}
 *     public SpringBasedCoherenceSession session()
 *     {
 *         return new SpringBasedCoherenceSession("cache-config-file.xml);
 *     }
 *
 *     {@literal @Bean}
 *     public NamedCache accounts()
 *     {
 *         return session.getCache("accounts");
 *     }
 * }
 * </code></pre>
 *
 * Copyright (c) 2015-2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SpringBasedCoherenceSession implements ApplicationContextAware,
													ApplicationListener<ApplicationContextEvent>,
													DisposableBean,
													InitializingBean {
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

	private final CoherenceBeanExpressionResolver coherenceBeanExpressionResolver =
			new CoherenceBeanExpressionResolver(ParameterMacroExpressionParser.INSTANCE);

	/**
	 * Constructs a {@link SpringBasedCoherenceSession} that will
	 * auto-detect the cache configuration.
	 */
	public SpringBasedCoherenceSession() {
		this(DEFAULT_CACHE_CONFIG_URI);
	}


	/**
	 * Constructs a {@link SpringBasedCoherenceSession} using the specified
	 * cache configuration.
	 * @param cacheConfigURI  the URI of the cache configuration
	 */
	public SpringBasedCoherenceSession(String cacheConfigURI) {
		this.applicationContext       = null;
		this.configurableCacheFactory = null;
		this.cacheConfigURI           = cacheConfigURI;
	}


	/**
	 * Acquire the specified {@link NamedCache}.
	 * @param name  the name of the cache
	 * @return a {@link NamedCache}
	 */
	public NamedCache getCache(String name) {
		return this.configurableCacheFactory.ensureCache(name, null);
	}


	/**
	 * Acquire the specified {@link Service}.
	 * @param name  the name of the {@link Service}
	 * @return a {@link Service}
	 */
	public Service getService(String name) {
		return this.configurableCacheFactory.ensureService(name);
	}


	/**
	 * Obtain the {@link ResourceRegistry} for the {@link SpringBasedCoherenceSession}.
	 * @return the {@link ResourceRegistry}
	 */
	public ResourceRegistry getResourceRegistry() {
		return this.configurableCacheFactory.getResourceRegistry();
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			// we don't re-cycle / restart the ConfigurableCacheFactory
			// when the application is refreshed
		}
		else if (event instanceof ContextStartedEvent) {
			this.configurableCacheFactory.activate();
		}
	}

	public ExtensibleConfigurableCacheFactory getConfigurableCacheFactory() {
		return this.configurableCacheFactory;
	}

	@Override
	public void destroy() throws Exception {
		this.configurableCacheFactory.dispose();
		CacheFactory.getCacheFactoryBuilder().release(this.configurableCacheFactory);
		this.configurableCacheFactory = null;
		this.coherenceBeanExpressionResolver.cleanupParameterResolver();
	}


	@Override
	public void afterPropertiesSet() throws Exception {

		// establish the configuration dependencies for the
		// ConfigurableCacheFactory we're about to create
		ExtensibleConfigurableCacheFactory.Dependencies dependencies = this.cacheConfigURI.equals(DEFAULT_CACHE_CONFIG_URI)
																		? newInstance() : newInstance(this.cacheConfigURI);

		if (this.applicationContext != null) {
			// override the bean expression resolver with one that can also
			// resolve Coherence-based values eg: cache-name, manager-context etc.
			AutowireCapableBeanFactory beanFactory = this.applicationContext.getAutowireCapableBeanFactory();

			if (beanFactory instanceof ConfigurableBeanFactory) {
				((ConfigurableBeanFactory) beanFactory)
					.setBeanExpressionResolver(this.coherenceBeanExpressionResolver);
			}

			// register this application context as a resource so that the
			// ConfigurableCacheFactory can later use it
			// (say for injecting into Coherence-based instances)
			dependencies.getResourceRegistry().registerResource(ApplicationContext.class, this.applicationContext);
			// also register this application context as the default BeanFactory
			dependencies.getResourceRegistry().registerResource(BeanFactory.class, this.applicationContext);

		}


		// create the ConfigurableCacheFactory
		this.configurableCacheFactory = new ExtensibleConfigurableCacheFactory(dependencies);

		// register the ConfigurableCacheFactory with the CacheFactoryBuilder
		// so that anyone attempting to use the static CacheFactory methods
		// like "CacheFactory.getCache()" has a chance of using the same
		// configuration and caches!
		CacheFactory.getCacheFactoryBuilder().setConfigurableCacheFactory(this.configurableCacheFactory,
				this.cacheConfigURI,
				null,
				false);
	}
}
