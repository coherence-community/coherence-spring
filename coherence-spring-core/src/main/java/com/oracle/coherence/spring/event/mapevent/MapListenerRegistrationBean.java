/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.oracle.coherence.common.base.Exceptions;
import com.oracle.coherence.spring.annotation.event.Created;
import com.oracle.coherence.spring.configuration.FilterService;
import com.oracle.coherence.spring.configuration.MapEventTransformerService;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.util.Filter;
import com.tangosol.util.MapEventTransformer;
import com.tangosol.util.MapListener;
import com.tangosol.util.filter.MapEventFilter;
import com.tangosol.util.filter.MapEventTransformerFilter;

/**
 * {@link CoherenceEventListener} responsible for the registration of {@link MapListener}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 *
 */
public class MapListenerRegistrationBean {

	private FilterService filterService;
	private MapEventTransformerService mapEventTransformerService;

	/**
	 * A list of event interceptors for all discovered observer methods.
	 */
	private final Map<String, Map<String, Set<AnnotatedMapListener<?, ?>>>> mapListeners = new HashMap<>();

	public MapListenerRegistrationBean(
			FilterService filterService,
			MapEventTransformerService mapEventTransformerService) {
		super();
		this.filterService = filterService;
		this.mapEventTransformerService = mapEventTransformerService;
	}

	public void registerMapListeners(String cacheName, String scopeName, String sessionName, String serviceName) {
		Set<AnnotatedMapListener<?, ?>> setListeners = getMapListeners(removeScope(serviceName), cacheName);

		Session session = Coherence.findSession(sessionName)
				.orElseThrow(() -> new IllegalStateException("Cannot find a Session with name " + sessionName));
		NamedCache cache = session.getCache(cacheName);

		for (AnnotatedMapListener<?, ?> listener : setListeners) {
			if (listener.hasFilterAnnotation()) {
				// ensure that the listener's filter has been resolved as this
				// was not possible as discovery time.
				listener.setFilter(this.filterService.resolve(listener.getFilterAnnotations()));
			}

			if (listener.hasTransformerAnnotation()) {
				// ensure that the listener's transformer has been resolved as this
				// was not possible at discovery time.
				listener.resolveTransformer(this.mapEventTransformerService);
			}

			String sScope = listener.getScopeName();
			boolean fScopeOK = sScope == null || sScope.equals(scopeName);
			String sSession = listener.getSession();
			boolean fSessionOK = sSession == null || sSession.equals(sessionName);

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
					boolean fLite = listener.isLiteEvent();
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

		registerMapListeners(cacheName, eventScope, eventSession, eventService);
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

	/**
	 * Add specified listener to the collection of discovered observer-based listeners.
	 * @param listener the listener to add
	 */
	public void addMapListener(AnnotatedMapListener<?, ?> listener) {
		final String serviceName = listener.getServiceName();
		final String cacheName = listener.getCacheName();

		final Map<String, Set<AnnotatedMapListener<?, ?>>> mapByCache = this.mapListeners.computeIfAbsent(serviceName, (s) -> new HashMap<>());
		final Set<AnnotatedMapListener<?, ?>> setListeners = mapByCache.computeIfAbsent(cacheName, (c) -> new HashSet<>());
		setListeners.add(listener);
	}
}
