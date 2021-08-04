/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.time.Duration;

import org.springframework.util.Assert;

/**
 * Defines additional configuration properties for the {@link CoherenceCache}. Can also serve as default cache configuration
 * via {@link CoherenceCacheManager}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceCacheConfiguration {
	private final Duration timeToLive;

	/**
	 * Initialize the cache configuration properties.
	 * @param timeToLive the expiration time for cache entries. Set it to {@link Duration#ZERO} to not expire entries or to
	 *                   control the expiration via the coherence-cache-config.xml file.
	 */
	public CoherenceCacheConfiguration(Duration timeToLive) {
		Assert.notNull(timeToLive, "timeToLive must not be null.");
		Assert.isTrue(!timeToLive.isNegative(), "timeToLive must not be negative.");
		this.timeToLive = timeToLive;
	}

	/**
	 * Returns the expiration time for cache entries. Should never be null.
	 * @return the time-to-live (TTL) for cache entries.
	 */
	public Duration getTimeToLive() {
		return this.timeToLive;
	}
}
