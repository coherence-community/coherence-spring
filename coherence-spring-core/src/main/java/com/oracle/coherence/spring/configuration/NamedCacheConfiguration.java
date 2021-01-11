/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.inject.SessionName;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Provides support for injecting Coherence Caches using {@link NamedCache}.
 *
 * @author Gunnar Hillert
 */
@Configuration
public class NamedCacheConfiguration {

	@Bean(destroyMethod = "release")
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V> NamedCache<K, V> getCache(InjectionPoint injectionPoint) {
		return getCacheInternal(injectionPoint, false);
	}

	private <K, V> NamedCache<K, V> getCacheInternal(InjectionPoint injectionPoint, boolean isCQC) {

		final Name cacheNameAnnotation = injectionPoint.getAnnotation(Name.class);
		final SessionName sessionNameAnnotation = injectionPoint.getAnnotation(SessionName.class);

		final String sessionName;
		final String cacheName;

		if (cacheNameAnnotation == null) {
			cacheName = null;
		}
		else {
			cacheName = cacheNameAnnotation.value();
		}

		if (sessionNameAnnotation == null) {
			sessionName = Coherence.DEFAULT_NAME;
		}
		else {
			sessionName = sessionNameAnnotation.value();
		}

		if (cacheName == null || cacheName.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot determine cache/map name. No @Name qualifier and injection point is not named");
		}

		Session session = Coherence.findSession(sessionName)
				.orElseThrow(() -> new IllegalStateException("No Session is configured with name " + sessionName));

		NamedCache<K, V> cache = session.getCache(cacheName);

//		if (isCQC || metadata.hasAnnotation(View.class)) {
//			boolean hasValues = metadata.booleanValue(View.class, "cacheValues").orElse(true);
//			Filter filter = filterFactory.filter(injectionPoint);
//			ValueExtractor extractor = null; // ToDo: createExtractor(qualifiers);
//			ViewId id = new ViewId(name, sessionName, filter, hasValues, extractor);
//
//			WeakReference<ContinuousQueryCache> refCQC = views.compute(id, (key, ref) -> {
//				ContinuousQueryCache cqc = ref == null ? null : ref.get();
//				if (cqc == null || !cqc.isActive()) {
//					cqc = new ContinuousQueryCache<>(cache, filter, hasValues, null, extractor);
//					return new WeakReference<>(cqc);
//				} else {
//					return ref;
//				}
//			});
//
//			return refCQC.get();
//		} else {
			return cache;
//		}
	}

}
