/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.time.Duration;
import java.util.concurrent.Callable;

import org.springframework.util.Assert;

/**
 * Defines additional configuration properties for the {@link CoherenceCache}. Can also serve as default cache configuration
 * via {@link CoherenceCacheManager}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceCacheConfiguration {

	/**
	 * see {@link #getTimeToLive()}.
	 */
	private Duration timeToLive = Duration.ZERO;

	/**
	 * see {@link #isUseCacheNamePrefix()}.
	 */
	private boolean useCacheNamePrefix = false;

	/**
	 * see {@link #isUseLocks()}.
	 */
	private boolean useLocks = true;

	/**
	 * see {@link #isLockEntireCache()}.
	 */
	private boolean lockEntireCache = false;

	/**
	 * see {@link #getLockTimeout()}.
	 */
	private long lockTimeout = 0;

	/**
	 * see {@link #getCacheNamePrefix()}.
	 */
	private String cacheNamePrefix = "";

	/**
	 * The default constructor.
	 */
	public CoherenceCacheConfiguration() {
	}

	/**
	 * Initialize the cache configuration properties.
	 * @param timeToLive the expiration time for cache entries. Set this property to {@link Duration#ZERO} to control
	 *                   the expiration via the coherence-cache-config.xml file. A value of -1 milliseconds will cause
	 *                   cache entries not to expire.
	 */
	public CoherenceCacheConfiguration(Duration timeToLive) {
		this.setTimeToLive(timeToLive);
	}

	/**
	 * Returns the expiration time for cache entries. Should never be null. A value of zero milliseconds means that the
	 * cache's default expiration value shall be used, e.g. to control the expiration via the coherence-cache-config.xml
	 * file. A value of -1 milliseconds means that the cache value will never expire.
	 * @return the time-to-live (TTL) for cache entries. Defaults to {@link Duration#ZERO}.
	 */
	public Duration getTimeToLive() {
		return this.timeToLive;
	}

	/**
	 * Sets the expiration time for cache entries. If not set, it will default to {@link Duration#ZERO}.
	 * @param timeToLive must neither be null nor smaller than -1 milliseconds.
	 */
	public void setTimeToLive(Duration timeToLive) {
		Assert.notNull(timeToLive, "timeToLive must not be null.");
		Assert.isTrue(timeToLive.toMillis() >= -1, "timeToLive must be bigger than -1.");
		this.timeToLive = timeToLive;
	}

	/**
	 * This property is false by default. If true, the cache name will be prefixed with the name specified by
	 * {@link #getCacheName(String)}.
	 * @return true if the {@link #getCacheNamePrefix()} is to be used
	 */
	public boolean isUseCacheNamePrefix() {
		return this.useCacheNamePrefix;
	}

	/**
	 * This property is false by default. If set to true, the cache name will be prefixed with the name specified by
	 * {@link #getCacheName(String)}.
	 * @param useCacheNamePrefix shall a cache name prefix be used? If not specified, this property defaults to false.
	 */
	public void setUseCacheNamePrefix(boolean useCacheNamePrefix) {
		this.useCacheNamePrefix = useCacheNamePrefix;
	}

	/**
	 * The String to prepend cache names with. Empty by default. Will be ignored if {@link #isUseCacheNamePrefix()} is
	 * false.
	 * @return the cache name prefix
	 */
	public String getCacheNamePrefix() {
		return this.cacheNamePrefix;
	}

	/**
	 * Set the String to prepend the cache name with. If not set will default to an empty String.
	 * @param cacheNamePrefix the cache name prefix
	 */
	public void setCacheNamePrefix(String cacheNamePrefix) {
		this.cacheNamePrefix = cacheNamePrefix;
	}

	/**
	 * Return the full cache name for the provided base cache name. If {@link #isUseCacheNamePrefix()} returns true,
	 * the cache name will be prepended by {@link #getCacheNamePrefix()}.
	 * @param name must not be null or empty
	 * @return the full cache name including prefix if enabled
	 */
	public String getCacheName(String name) {
		Assert.hasText(name, "The provide cache name must not be null nor empty.");
		if (this.isUseCacheNamePrefix()) {
			return this.getCacheNamePrefix() + name;
		}
		else {
			return name;
		}
	}

	/**
	 * If true, cache entries will be locked for concurrency control. This property returns true by default.
	 * When using Coherence*Extend or gRPC, it is recommended to not use locking. This property is used by the value-loader in
	 * {@link CoherenceCache#get(Object, Callable)}.
	 * @return if not set, will return true
	 */
	public boolean isUseLocks() {
		return this.useLocks;
	}

	/**
	 * If true, cache entries will be locked for concurrency control. This property is set to true by default.
	 * When using Coherence*Extend or gRPC, it is recommended to set this property to false.
	 * @param useLocks if not set will default to true
	 */
	public void setUseLocks(boolean useLocks) {
		this.useLocks = useLocks;
	}

	/**
	 * If returning true, this property will lock the entire cache. This is usually not recommended.
	 * @return true if the entire cache shall be locked. This property is used by the value-loader in
	 * {@link CoherenceCache#get(Object, Callable)}.
	 */
	public boolean isLockEntireCache() {
		return this.lockEntireCache;
	}

	/**
	 * If setting to true, this property will lock the entire cache. This is usually not recommended.
	 * @param lockEntireCache if not set defaults to false
	 */
	public void setLockEntireCache(boolean lockEntireCache) {
		this.lockEntireCache = lockEntireCache;
	}

	/**
	 * Returns the number of milliseconds to continue trying to obtain a lock. If zero is returned, the lock attempt
	 * will return immediately. A value of -1 means that the underlying locking operation will block indefinitely until the
	 * lock could be obtained. Defaults to 0. This property is used by the value-loader in {@link CoherenceCache#get(Object, Callable)}.
	 * @return the lock timeout in milliseconds
	 */
	public long getLockTimeout() {
		return this.lockTimeout;
	}

	/**
	 * Will set the lock timeout in milliseconds. If set to zero is returned, the lock attempt
	 * will return immediately. A value of -1 means that the underlying locking operation will block indefinitely until the
	 * lock could be obtained. If not set, this property defaults to 0.
	 * @param lockTimeout the lock timeout in milliseconds
	 */
	public void setLockTimeout(long lockTimeout) {
		this.lockTimeout = lockTimeout;
	}
}
