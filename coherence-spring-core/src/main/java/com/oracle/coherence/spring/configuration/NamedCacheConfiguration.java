/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.reflect.Field;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.View;
import com.tangosol.net.AsyncNamedCache;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * Provides support for injecting Coherence Caches using {@link NamedCache}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
public class NamedCacheConfiguration {
	protected static final Log logger = LogFactory.getLog(NamedCacheConfiguration.class);

	/**
	 * The name of the default {@link Coherence} cache bean.
	 */
	public static final String COHERENCE_CACHE_BEAN_NAME = "getCache";

	/**
	 * The name of the default {@link Coherence} cache bean.
	 */
	public static final String COHERENCE_VIEW_BEAN_NAME = "getView";

	/**
	 * The name of the default {@link Coherence} cache bean.
	 */
	public static final String COHERENCE_ASYNC_CACHE_BEAN_NAME = "getAsyncCache";

	final FilterService filterService;
	final ExtractorService extractorService;

	public NamedCacheConfiguration(FilterService filterService, ExtractorService extractorService) {
		this.filterService = filterService;
		this.extractorService = extractorService;
	}

	@Bean(destroyMethod = "release")
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V_BACK, V_FRONT> ContinuousQueryCache<K, V_BACK, V_FRONT> getView(InjectionPoint injectionPoint) {
		return (ContinuousQueryCache<K, V_BACK, V_FRONT>) getCacheInternal(injectionPoint, true);
	}

	@Bean(destroyMethod = "release")
	@DependsOn(CoherenceSpringConfiguration.COHERENCE_SERVER_BEAN_NAME)
	@Primary
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V> NamedCache<K, V> getCache(InjectionPoint injectionPoint) {
		return getCacheInternal(injectionPoint, false);
	}

	/**
	 * Create a {@link NamedCache} instance.
	 * <p>
	 * If the injection point has a type of {@link ContinuousQueryCache} or is qualified with the {@link View}
	 * annotation then a {@link ContinuousQueryCache} instance will be returned otherwise a {@link NamedCache} will be
	 * returned.
	 * @param injectionPoint the {@link InjectionPoint} that the {@link NamedCache} will be retrieved from
	 * @param isCQC a flag specifying whether to return a {@link ContinuousQueryCache}
	 * @param <K> the type of the cache keys
	 * @param <V> the type of the cache values
	 * @return a {@link NamedCache} instance to inject into the injection point
	 */
	private <K, V> NamedCache<K, V> getCacheInternal(InjectionPoint injectionPoint, boolean isCQC) {

		final SessionName sessionNameAnnotation = injectionPoint.getAnnotation(SessionName.class);
		final View viewAnnotation = injectionPoint.getAnnotation(View.class);

		final String sessionName;
		final String cacheName = this.determineCacheName(injectionPoint);

		if (sessionNameAnnotation == null) {
			sessionName = Coherence.DEFAULT_NAME;
		}
		else {
			sessionName = sessionNameAnnotation.value();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Going to retrieve NamedCache '%s' for session '%s'.", cacheName, sessionName));
		}

		if (cacheName == null || cacheName.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot determine cache/map name. No @Name qualifier and injection point is not named");
		}

		final Session session = Coherence.findSession(sessionName)
				.orElseThrow(() -> new IllegalStateException(String.format("No Session is configured with name '%s'.", sessionName)));

		final NamedCache<K, V> cache = session.getCache(cacheName);

		if (isCQC || viewAnnotation != null) {
			boolean hasValues = (viewAnnotation == null) || viewAnnotation.cacheValues();

			final Filter filter = this.filterService.getFilter(injectionPoint);
			final ValueExtractor extractor = this.extractorService.getExtractor(injectionPoint, true);

			return new ContinuousQueryCache<>(cache, filter, hasValues, null, extractor);

		}
		return cache;
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V> AsyncNamedCache<K, V> getAsyncCache(InjectionPoint injectionPoint) {
		NamedCache<K, V> cache = getCache(injectionPoint);
		return cache.async();
	}

	private String determineCacheName(InjectionPoint injectionPoint) {
		final String cacheName;
		final Name cacheNameAnnotation = injectionPoint.getAnnotation(Name.class);
		if (cacheNameAnnotation == null) {
			final Field field = injectionPoint.getField();

			if (field == null) {
				throw new IllegalStateException("Not an annotated field.");
			}
			else {
				cacheName = field.getName();
			}
		}
		else {
			cacheName = cacheNameAnnotation.value();
		}
		return cacheName;
	}
}
