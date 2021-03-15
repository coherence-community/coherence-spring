/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.oracle.coherence.common.base.Exceptions;
import com.oracle.coherence.spring.annotation.event.Created;
import com.oracle.coherence.spring.configuration.FilterService;
import com.oracle.coherence.spring.configuration.MapEventTransformerService;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.internal.NamedEventInterceptor;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.util.Filter;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.SafeLinkedList;
import com.tangosol.util.filter.MapEventFilter;
import com.tangosol.util.filter.MapEventTransformerFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.framework.autoproxy.AutoProxyUtils;
import org.springframework.aop.scope.ScopedObject;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

/**
 * A {@link BeanFactoryPostProcessor} that processes methods annotated with
 * {@literal @}{@link com.oracle.coherence.spring.event.CoherenceEventListener}.
 * Similar to {@link org.springframework.context.event.EventListenerMethodProcessor}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 *
 */
public class CoherenceEventListenerMethodProcessor implements ApplicationContextAware, BeanFactoryPostProcessor {
	protected final Log logger = LogFactory.getLog(getClass());

	@SuppressWarnings("unchecked")
	private final List<NamedEventInterceptor<?>> interceptors = new SafeLinkedList();

	/**
	 * A list of event interceptors for all discovered observer methods.
	 */
	private final Map<String, Map<String, Set<AnnotatedMapListener<?, ?>>>> mapListeners = new HashMap<>();

	@Nullable
	private ConfigurableApplicationContext applicationContext;

	@Nullable
	private ConfigurableListableBeanFactory beanFactory;

	private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		Assert.isTrue(applicationContext instanceof ConfigurableApplicationContext,
				"ApplicationContext does not implement ConfigurableApplicationContext");
		this.applicationContext = (ConfigurableApplicationContext) applicationContext;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;

		Assert.state(this.beanFactory != null, "No ConfigurableListableBeanFactory set");
		String[] beanNames = beanFactory.getBeanNamesForType(Object.class);
		for (String beanName : beanNames) {
			if (!ScopedProxyUtils.isScopedTarget(beanName)) {
				Class<?> type = null;
				try {
					type = AutoProxyUtils.determineTargetClass(beanFactory, beanName);
				}
				catch (Throwable ex) {
					// An unresolvable bean type, probably from a lazy bean - let's ignore it.
					if (this.logger.isDebugEnabled()) {
						this.logger.debug("Could not resolve target class for bean with name '" + beanName + "'", ex);
					}
				}
				if (type != null) {
					if (ScopedObject.class.isAssignableFrom(type)) {
						try {
							Class<?> targetClass = AutoProxyUtils.determineTargetClass(
									beanFactory, ScopedProxyUtils.getTargetBeanName(beanName));
							if (targetClass != null) {
								type = targetClass;
							}
						}
						catch (Throwable ex) {
							// An invalid scoped proxy arrangement - let's ignore it.
							if (this.logger.isDebugEnabled()) {
								this.logger.debug("Could not resolve target bean for scoped proxy '" + beanName + "'", ex);
							}
						}
					}
					try {
						processBean(beanName, type);
					}
					catch (Throwable ex) {
						throw new BeanInitializationException("Failed to process @EventListener " +
								"annotation on bean with name '" + beanName + "'", ex);
					}
				}
			}
		}
	}

	private void processBean(final String beanName, final Class<?> targetType) {
		if (!this.nonAnnotatedClasses.contains(targetType) &&
				AnnotationUtils.isCandidateClass(targetType, CoherenceEventListener.class) &&
				!isSpringContainerClass(targetType)) {

			Map<Method, CoherenceEventListener> annotatedMethods = null;
			try {
				final MethodIntrospector.MetadataLookup<CoherenceEventListener> metadataLookup = (method) ->
								AnnotatedElementUtils.findMergedAnnotation(method, CoherenceEventListener.class);
				annotatedMethods = MethodIntrospector.selectMethods(targetType, metadataLookup);
			}
			catch (Throwable ex) {
				// An unresolvable type in a method signature, probably from a lazy bean - let's ignore it.
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Could not resolve methods for bean with name '" + beanName + "'", ex);
				}
			}

			if (CollectionUtils.isEmpty(annotatedMethods)) {
				this.nonAnnotatedClasses.add(targetType);
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("No @CoherenceEventListener annotations found on bean class: " + targetType.getName());
				}
			}
			else {
				// Non-empty set of methods
				ConfigurableApplicationContext context = this.applicationContext;
				Assert.state(context != null, "No ApplicationContext set");
				//List<CoherenceEventListenerFactory> factories = this.eventListenerFactories;
				//Assert.state(factories != null, "CoherenceEventListenerFactory List not initialized");
				for (Method method : annotatedMethods.keySet()) {
					final List<Parameter> arguments = Arrays.asList(method.getParameters());
					Class<?> argumentClassType = (arguments.size() == 1) ? arguments.get(0).getType() : null;

					if (argumentClassType == null || (!Event.class.isAssignableFrom(argumentClassType)
							&& !MapEvent.class.isAssignableFrom(argumentClassType))) {
						throw new IllegalArgumentException("The @CoherenceEventListener annotated method "
								+ method.getName() + " must have a single Coherence Event or MapEvent argument.");
					}

					//Class<?> clsBeanType = beanDefinition.getBeanType();
					final Object bean = this.applicationContext.getBean(beanName);
					this.logger.isInfoEnabled();
					this.logger.info(String.format("Handling Coherence %s - Bean: %s, type: %s, method: %s", argumentClassType.getName(), beanName, targetType.getName(), method.getName()));

					if (Event.class.isAssignableFrom(argumentClassType)) {
						MethodEventObserver observer = new MethodEventObserver(bean, method);
						EventObserverSupport.EventHandler handler = EventObserverSupport
								.createObserver((Class<? extends Event>) argumentClassType, observer);
						NamedEventInterceptor interceptor = new NamedEventInterceptor(observer.getId(), handler);
						this.interceptors.add(interceptor);
					}
					else {
						// type is MapEvent
						MethodMapListener listener = new MethodMapListener(bean, method);
						AnnotatedMapListener mapListener = new AnnotatedMapListener(listener, listener.getObservedQualifiers());
						addMapListener(mapListener);
					}
				}
				if (this.logger.isDebugEnabled()) {
					this.logger.debug(annotatedMethods.size() + " @CoherenceEventListener methods processed on bean '" +
							beanName + "': " + annotatedMethods);
				}
			}
		}
	}

	/**
	 * Determine whether the given class is an {@code org.springframework}
	 * bean class that is not annotated as a user or test {@link Component}...
	 * which indicates that there is no {@link CoherenceEventListener} to be found there.
	 * @param clazz the class to check
	 * @return true if the class is in the {@code org.springframework.} package and not annotated with {@link Component}
	 */
	private static boolean isSpringContainerClass(Class<?> clazz) {
		return (clazz.getName().startsWith("org.springframework.") &&
				!AnnotatedElementUtils.isAnnotated(ClassUtils.getUserClass(clazz), Component.class));
	}

	public List<NamedEventInterceptor<?>> getInterceptors() {
		return this.interceptors;
	}

	/**
	 * Listen for {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent.Type#CREATED Created}
	 * {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent CacheLifecycleEvents}
	 * and register relevant map listeners when caches are created.
	 * @param event the {@link com.tangosol.net.events.partition.cache.CacheLifecycleEvent}
	 */
	@CoherenceEventListener
	@SuppressWarnings({"rawtypes", "unchecked"})
	void registerMapListeners(@Created CacheLifecycleEvent event) {
		String cacheName = event.getCacheName();
		String eventScope = event.getScopeName();
		String eventSession = event.getSessionName();
		String eventService = event.getServiceName();

		Set<AnnotatedMapListener<?, ?>> setListeners = getMapListeners(removeScope(eventService), cacheName);

		Session session = Coherence.findSession(eventSession)
				.orElseThrow(() -> new IllegalStateException("Cannot find a Session with name " + eventSession));
		NamedCache cache = session.getCache(cacheName);

		for (AnnotatedMapListener<?, ?> listener : setListeners) {
			if (listener.hasFilterAnnotation()) {
				// ensure that the listener's filter has been resolved as this
				// was not possible as discovery time.
				listener.resolveFilter(this.applicationContext.getBean(FilterService.class));
			}

			if (listener.hasTransformerAnnotation()) {
				// ensure that the listener's transformer has been resolved as this
				// was not possible as discovery time.
				listener.resolveTransformer(this.applicationContext.getBean(MapEventTransformerService.class));
			}

			String sScope = listener.getScopeName();
			boolean fScopeOK = sScope == null || sScope.equals(eventScope);
			String sSession = listener.getSessionName();
			boolean fSessionOK = sSession == null || sSession.equals(eventSession);

			if (fScopeOK && fSessionOK) {
				Filter filter = listener.getFilter();
				if (filter != null && !(filter instanceof MapEventFilter)) {
					filter = new MapEventFilter(MapEventFilter.E_ALL, filter);
				}

				MapEventTransformer transformer = listener.getTransformer();
				if (transformer != null) {
					filter = new MapEventTransformerFilter(filter, transformer);
				}

				try {
					boolean fLite = listener.isLite();
					if (listener.isSynchronous()) {
						cache.addMapListener(listener.synchronous(), filter, fLite);
					}
					else {
						cache.addMapListener(listener, filter, fLite);
					}
				}
				catch (Exception ex) {
					throw Exceptions.ensureRuntimeException(ex);
				}
			}
		}
	}

	/**
	 * Remove the scope prefix from a specified service name.
	 * @param sServiceName the service name to remove scope prefix from
	 * @return service name with scope prefix removed
	 */
	private String removeScope(String sServiceName) {
		if (sServiceName == null) {
			return "";
		}
		int nIndex = sServiceName.indexOf(':');
		return (nIndex > -1) ? sServiceName.substring(nIndex + 1) : sServiceName;
	}

	/**
	 * Add specified listener to the collection of discovered observer-based listeners.
	 * @param listener the listener to add
	 */
	public void addMapListener(AnnotatedMapListener<?, ?> listener) {
		String svc = listener.getServiceName();
		String cache = listener.getCacheName();

		Map<String, Set<AnnotatedMapListener<?, ?>>> mapByCache = this.mapListeners.computeIfAbsent(svc, (s) -> new HashMap<>());
		Set<AnnotatedMapListener<?, ?>> setListeners = mapByCache.computeIfAbsent(cache, (c) -> new HashSet<>());
		setListeners.add(listener);
	}

	/**
	 * Return all map listeners that should be registered for a particular
	 * service and cache combination.
	 * @param serviceName the name of the service
	 * @param cacheName   the name of the cache
	 * @return a set of all listeners that should be registered
	 */
	public Set<AnnotatedMapListener<?, ?>> getMapListeners(String serviceName, String cacheName) {
		HashSet<AnnotatedMapListener<?, ?>> setResults = new HashSet<>();
		collectMapListeners(setResults, "*", "*");
		collectMapListeners(setResults, "*", cacheName);
		collectMapListeners(setResults, serviceName, "*");
		collectMapListeners(setResults, serviceName, cacheName);

		return setResults;
	}

	/**
	 * Return all map listeners that should be registered against a specific
	 * remote cache or map in a specific session.
	 * @return all map listeners that should be registered against a
	 * specific cache or map in a specific session
	 */
	public Collection<AnnotatedMapListener<?, ?>> getNonWildcardMapListeners() {
		return this.mapListeners.values()
				.stream()
				.flatMap((map) -> map.values().stream())
				.flatMap(Set::stream)
				.filter((listener) -> listener.getSessionName() != null)
				.filter((listener) -> !listener.isWildCardCacheName())
				.sorted()
				.collect(Collectors.toList());
	}

	/**
	 * Add all map listeners for the specified service and cache combination to
	 * the specified result set.
	 * @param setResults  the set of results to accumulate listeners into
	 * @param serviceName the name of the service
	 * @param cacheName   the name of the cache
	 */
	private void collectMapListeners(HashSet<AnnotatedMapListener<?, ?>> setResults, String serviceName, String cacheName) {
		Map<String, Set<AnnotatedMapListener<?, ?>>> mapByCache = this.mapListeners.get(serviceName);
		if (mapByCache != null) {
			setResults.addAll(mapByCache.getOrDefault(cacheName, Collections.emptySet()));
		}
	}
}
