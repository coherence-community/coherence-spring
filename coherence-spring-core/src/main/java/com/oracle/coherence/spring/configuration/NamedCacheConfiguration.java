/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.FilterBinding;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.View;
import com.oracle.coherence.spring.event.mapevent.MapListenerRegistrationBean;
import com.tangosol.net.AsyncNamedCache;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.StringUtils;

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
	public static final String COHERENCE_CACHE_BEAN_NAME = "getCacheProxy";

	/**
	 * The name of the default {@link Coherence} cache bean.
	 */
	public static final String COHERENCE_VIEW_BEAN_NAME = "getViewProxy";

	/**
	 * The name of the default {@link Coherence} cache bean.
	 */
	public static final String COHERENCE_ASYNC_CACHE_BEAN_NAME = "getAsyncCacheProxy";

	/**
	 * The name of the factory bean used to create Coherence caches/maps.
	 */
	private static final String FACTORY_BEAN_NAME = NamedCacheConfiguration.class.getCanonicalName();

	final FilterService filterService;
	final ExtractorService extractorService;

	final Map<CacheId, NamedCache> cacheInstances = new ConcurrentHashMap<>();

	final Map<CqcId, ContinuousQueryCache> cqcInstances = new ConcurrentHashMap<>();

	final MapListenerRegistrationBean mapListenerRegistrationBean;

	final BeanFactory beanFactory;

	public NamedCacheConfiguration(FilterService filterService, ExtractorService extractorService, MapListenerRegistrationBean mapListenerRegistrationBean,
			BeanFactory beanFactory) {
		this.filterService = filterService;
		this.extractorService = extractorService;
		this.mapListenerRegistrationBean = mapListenerRegistrationBean;
		this.beanFactory = beanFactory;
	}

	@Bean(destroyMethod = "release")
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V_BACK, V_FRONT> ContinuousQueryCache<K, V_BACK, V_FRONT> getViewProxy(InjectionPoint injectionPoint) {
		LazyTargetSource targetSource = new LazyTargetSource(
				(DefaultListableBeanFactory) this.beanFactory,
				injectionPoint,
				FACTORY_BEAN_NAME,
				"getView", // Name of the factory method
				false);

		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(targetSource);
		pf.setProxyTargetClass(true);
		return (ContinuousQueryCache<K, V_BACK, V_FRONT>) pf.getProxy();
	}

	@Bean(destroyMethod = "release", autowireCandidate = false)
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V_BACK, V_FRONT> ContinuousQueryCache<K, V_BACK, V_FRONT> getView(InjectionPoint injectionPoint) {
		return (ContinuousQueryCache<K, V_BACK, V_FRONT>) getCacheInternal(injectionPoint, true);
	}

	@Bean(destroyMethod = "release")
	@DependsOn(CoherenceSpringConfiguration.COHERENCE_SERVER_BEAN_NAME)
	@Primary
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V> NamedCache<K, V> getCacheProxy(InjectionPoint injectionPoint) {
		boolean isView = hasViewAnnotation(injectionPoint) || ContinuousQueryCache.class.isAssignableFrom(injectionPoint.getDeclaredType());
		if (isView) {
			return getViewProxy(injectionPoint);
		}

		LazyTargetSource targetSource = new LazyTargetSource(
				(DefaultListableBeanFactory) this.beanFactory,
				injectionPoint,
				FACTORY_BEAN_NAME,
				"getCache", // Name of the factory method
				false);

		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(targetSource);
		pf.setInterfaces(NamedCache.class);
		return (NamedCache<K, V>) pf.getProxy();
	}

	@Bean(destroyMethod = "release", autowireCandidate = false)
	@DependsOn(CoherenceSpringConfiguration.COHERENCE_SERVER_BEAN_NAME)
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
		final MergedAnnotations mergedAnnotations = getMergedAnnotations(injectionPoint);
		final MergedAnnotation<Name> mergedNameAnnotation = mergedAnnotations.get(Name.class);
		final MergedAnnotation<SessionName> mergedSessionNameAnnotation = mergedAnnotations.get(SessionName.class);
		final MergedAnnotation<View> mergedViewAnnotation = mergedAnnotations.get(View.class);

		final String sessionName;
		final String cacheName = this.determineCacheName(injectionPoint, mergedNameAnnotation);

		if (!mergedSessionNameAnnotation.isPresent() || mergedSessionNameAnnotation.synthesize().value().trim().isEmpty()) {
			sessionName = Coherence.DEFAULT_NAME;
		}
		else {
			sessionName = mergedSessionNameAnnotation.synthesize().value();
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

		CacheId cacheId = new CacheId(cacheName, session.getScopeName(), session.getName());
		NamedCache<K, V> cache = this.cacheInstances.compute(cacheId, (id, current) -> {
			if (current != null && current.isActive()) {
				return current;
			}
			NamedCache<K, V> c = session.getCache(cacheName);
			this.mapListenerRegistrationBean.registerMapListeners(cacheName, session.getScopeName(), sessionName, c.getCacheService().getInfo().getServiceName());
			return c;
		});

		if (isCQC || mergedViewAnnotation.isPresent()) {
			Set<Annotation> qualifiers = extractQualifiers(injectionPoint);
			CqcId cqcId = new CqcId(cacheName, session.getScopeName(), session.getName(), qualifiers);
			return this.cqcInstances.compute(cqcId, (id, current) -> {
				if (current != null && current.isActive()) {
					return current;
				}
				boolean hasValues = !mergedViewAnnotation.isPresent() || mergedViewAnnotation.synthesize().cacheValues();
				Filter filter = this.filterService.getFilter(injectionPoint);
				ValueExtractor extractor = this.extractorService.getExtractor(injectionPoint, true);
				return new ContinuousQueryCache<>(cache, filter, hasValues, null, extractor);
			});
		}
		return cache;
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<K, V> AsyncNamedCache<K, V> getAsyncCacheProxy(InjectionPoint injectionPoint) {
		boolean isView = hasViewAnnotation(injectionPoint) || ContinuousQueryCache.class.isAssignableFrom(injectionPoint.getDeclaredType());
		String factoryMethod = isView ? "getView" : "getCache";

		// Create the custom TargetSource
		LazyTargetSource targetSource = new LazyTargetSource(
				(DefaultListableBeanFactory) this.beanFactory,
				injectionPoint, // Pass the injection point where the proxy is being injected
				FACTORY_BEAN_NAME,
				factoryMethod,
				true);

		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(targetSource);
		pf.setInterfaces(AsyncNamedCache.class);
		return (AsyncNamedCache<K, V>) pf.getProxy();
	}

	private boolean hasViewAnnotation(InjectionPoint injectionPoint) {
		MergedAnnotations mergedAnnotations = getMergedAnnotations(injectionPoint);
		MergedAnnotation<View> mergedViewAnnotation = mergedAnnotations.get(View.class);
		return mergedViewAnnotation.isPresent();
	}

	private MergedAnnotations getMergedAnnotations(InjectionPoint injectionPoint) {
		final MergedAnnotations mergedAnnotations;
		final MethodParameter methodParameter = injectionPoint.getMethodParameter();
		if (methodParameter != null && methodParameter.getParameterAnnotations() != null) {
			mergedAnnotations = MergedAnnotations.from(methodParameter.getParameterAnnotations());
		}
		else {
			mergedAnnotations = MergedAnnotations.from(injectionPoint.getAnnotatedElement());
		}
		return mergedAnnotations;
	}

	private Set<Annotation> extractQualifiers(InjectionPoint injectionPoint) {
		Annotation[] annotations = injectionPoint.getAnnotations();
		if (annotations == null) {
			return Collections.emptySet();
		}
		return Arrays.stream(annotations)
				.filter((annotation) -> annotation.annotationType().isAnnotationPresent(FilterBinding.class)
						|| annotation.annotationType().isAnnotationPresent(ExtractorBinding.class)
						|| annotation.annotationType().isAnnotationPresent(View.class))
				.collect(Collectors.toSet());
	}

	private String determineCacheName(InjectionPoint injectionPoint, MergedAnnotation<Name> mergedNameAnnotation) {
		final String cacheName;

		if (!mergedNameAnnotation.isPresent() || !StringUtils.hasText(mergedNameAnnotation.synthesize().value())) {
			final Field field = injectionPoint.getField();
			if (field == null) {
				final MethodParameter methodParameter = injectionPoint.getMethodParameter();
				if (methodParameter != null) {
					methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
					final String parameterName = methodParameter.getParameterName();
					if (parameterName != null) {
						cacheName = parameterName;
					}
					else {
						throw new IllegalStateException("Unable to retrieve the name of the method parameter");
					}
				}
				else {
					throw new IllegalStateException("Not an annotated field nor a method parameter");
				}
			}
			else {
				cacheName = field.getName();
			}
		}
		else {
			cacheName = mergedNameAnnotation.synthesize().value();
		}
		return cacheName;
	}

	record CacheId(String name, String scope, String session) {
	}

	record CqcId(String name, String scope, String session, Set<Annotation> qualifiers) {
	}
}
