/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.cache;

import java.time.Duration;

import com.tangosol.net.NamedCache;
import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceCacheTests {

	@Test
	public void testBasicGet() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn("bar");
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		assertThat(coherenceCache.get("foo")).isInstanceOf(Cache.ValueWrapper.class);
		assertThat(coherenceCache.get("foo").get()).isEqualTo("bar");
	}

	@Test
	public void testSize() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.size()).thenReturn(1);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		assertThat(coherenceCache.size()).isEqualTo(1);
	}

	@Test
	public void testSettingOfCacheConfiguration() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ofMillis(9999)));
		assertThat(coherenceCache.getCacheConfiguration()).isNotNull();
		assertThat(coherenceCache.getCacheConfiguration().getTimeToLive()).isEqualTo(Duration.ofMillis(9999));
	}

	@Test
	public void testGettingNativeCache() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ofMillis(9999)));
		assertThat(coherenceCache.getNativeCache()).isNotNull();
		assertThat(coherenceCache.getNativeCache()).isSameAs(namedCache);
	}

	@Test
	public void testGetCorrectType() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn(new FooType("bar"));

		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ofMillis(9999)));
		assertThat(coherenceCache.get("foo")).isNotNull();
		assertThat(coherenceCache.get("foo")).isInstanceOf(Cache.ValueWrapper.class);
		assertThat(coherenceCache.get("foo").get()).isNotNull();
		assertThat(coherenceCache.get("foo").get()).isInstanceOf(FooType.class);
		assertThat(((FooType) coherenceCache.get("foo").get()).getMessage()).isEqualTo("bar");

		assertThat(coherenceCache.get("foo", FooType.class)).isNotNull();
		assertThat(coherenceCache.get("foo", FooType.class)).isInstanceOf(FooType.class);
		assertThat(coherenceCache.get("foo", FooType.class).getMessage()).isEqualTo("bar");
	}

	@Test
	public void testGetWrongType() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn(new FooType("bar"));

		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ofMillis(9999)));
		assertThatThrownBy(() -> coherenceCache.get("foo", String.class)).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("is not of required type 'java.lang.String'");
	}

	@Test
	public void testPutWithNullValue() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ofMillis(9999)));
		coherenceCache.put("foo", null);
		verify(namedCache, never()).put(any(), any(), anyLong());
		verify(namedCache, never()).put(any(), any());
	}

	@Test
	public void testPutWithTtl() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ofMillis(9999)));
		coherenceCache.put("foo", "bar");
		verify(namedCache, times(1)).put("foo", "bar", 9999);
		verify(namedCache, never()).put(any(), any());
	}

	@Test
	public void testPutWithZeroTtl() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		coherenceCache.put("foo", "bar");
		verify(namedCache, never()).put(any(), any(), anyLong());
		verify(namedCache, times(1)).put("foo", "bar");
	}

	@Test
	public void testGetNameOfCache() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.getCacheName()).thenReturn("foo_cache");
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		assertThat(coherenceCache.getName()).isNotNull();
		assertThat(coherenceCache.getName()).isEqualTo("foo_cache");
	}

	@Test
	public void testCacheEviction() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		coherenceCache.evict("foo");
		verify(namedCache, times(1)).remove("foo");
	}

	@Test
	public void testClearingOfCache() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		coherenceCache.clear();
		verify(namedCache, times(1)).clear();
	}

	@Test
	public void testGetUsingValueLoader() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn(null);
		when(namedCache.lock(eq("foo"), anyLong())).thenReturn(true);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		final String bar = coherenceCache.get("foo", () -> "bar");
		assertThat(bar).isEqualTo("bar");
		verify(namedCache, times(2)).get("foo");
	}

	@Test
	public void testGetUsingValueLoaderWithValueInCacheAfterLock() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn(null, "secondCallValue");
		when(namedCache.lock(eq("foo"), anyLong())).thenReturn(true);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		final String bar = coherenceCache.get("foo", () -> fail("Callable should not have been called."));
		assertThat(bar).isEqualTo("secondCallValue");
		verify(namedCache, times(2)).get("foo");
	}

	@Test
	public void testGetUsingValueLoaderWithValueInCache() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn("bar");

		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));
		final String bar = coherenceCache.get("foo", () -> fail("Callable should not have been called."));
		assertThat(bar).isEqualTo("bar");
	}

	@Test
	public void testGetUsingValueLoaderThrowingException() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn(null);
		when(namedCache.lock(eq("foo"), anyLong())).thenReturn(true);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));

		assertThatThrownBy(() -> {
			coherenceCache.get("foo", () -> {
				throw new IllegalStateException("No foo.");
			});
		}).isInstanceOf(Cache.ValueRetrievalException.class)
				.hasMessageContaining("Value for key 'foo' could not be loaded using");
	}

	@Test
	public void testGetWithValueLoaderAndLockFailure() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn(null);
		when(namedCache.lock(eq("foo"), eq(0))).thenReturn(false);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, new CoherenceCacheConfiguration(Duration.ZERO));

		try {
			coherenceCache.get("foo", () -> "bar");
		}
		catch (Cache.ValueRetrievalException ex) {
			assertThat(ex.getMessage()).contains("Value for key 'foo' could not be loaded using");
			assertThat(ex.getCause()).isInstanceOf(IllegalStateException.class);
			assertThat(ex.getCause().getMessage()).contains("Unable to lock key 'foo' within the specified timeout of 0ms.");
			return;
		}
		fail("Expected a ValueRetrievalException to be thrown.");
	}

	@Test
	public void testGetWithValueLoaderSpecifiedTimeoutAndLockFailure() {
		final NamedCache<Object, Object> namedCache = mock(NamedCache.class);
		when(namedCache.get("foo")).thenReturn(null);
		when(namedCache.lock(eq("foo"), eq(0))).thenReturn(false);
		final CoherenceCacheConfiguration config = new CoherenceCacheConfiguration(Duration.ZERO);
		config.setLockTimeout(1234);
		final CoherenceCache coherenceCache = new CoherenceCache(namedCache, config);

		try {
			coherenceCache.get("foo", () -> "bar");
		}
		catch (Cache.ValueRetrievalException ex) {
			assertThat(ex.getMessage()).contains("Value for key 'foo' could not be loaded using");
			assertThat(ex.getCause()).isInstanceOf(IllegalStateException.class);
			assertThat(ex.getCause().getMessage()).contains("Unable to lock key 'foo' within the specified timeout of 1234ms.");
			return;
		}
		fail("Expected a ValueRetrievalException to be thrown.");
	}

	final class FooType {
		final String message;

		private  FooType(String message) {
			this.message = message;
		}

		private String getMessage() {
			return this.message;
		}
	}
}
