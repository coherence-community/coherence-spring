/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.cache.CacheStatistics;
import com.tangosol.net.cache.NearCache;

/**
 * Explicit controller for retrieving cache statistics.
 *
 * @author Gunnar Hillert
 *
 */
@RestController
@Transactional()
public class StatisticsController {

	@Autowired
	private CacheManager cacheManager;

	@RequestMapping(path="/api/statistics/{cacheName}")
	@GetMapping
	public CacheStatistics getEvents(@PathVariable String cacheName) {
		final NearCache<?, ?> nearCache = (NearCache<?, ?>) CacheFactory.getCache(cacheName);

		final CacheStatistics cacheStatistics = nearCache.getCacheStatistics();

		return cacheStatistics;
	}

	@RequestMapping(path="/api/cache-names")
	@GetMapping
	public Collection<String> getCacheNames() {
		return cacheManager.getCacheNames();
	}
}
