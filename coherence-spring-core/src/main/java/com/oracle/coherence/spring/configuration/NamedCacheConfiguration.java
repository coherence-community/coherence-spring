/*
 * File: NamedCacheConfiguration.java
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
package com.oracle.coherence.spring.configuration;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.inject.SessionName;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

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
