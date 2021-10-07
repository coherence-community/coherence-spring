/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import java.util.Map;

import com.tangosol.net.NamedCache;
import com.tangosol.net.NamedCollection;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Releasable;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.AbstractKeyBasedMap;
import com.tangosol.util.AbstractKeySetBasedMap;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.QueryMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.core.convert.support.DefaultConversionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoherenceGenericConverterTests {

	private final DefaultConversionService conversionService = new DefaultConversionService();
	private final NamedCache mockedNamedCache = mock(NamedCache.class);
	private final ContinuousQueryCache mockedContinuousQueryCache = mock(ContinuousQueryCache.class);

	@BeforeEach
	public void setup() {
		this.conversionService.addConverter(new CoherenceGenericConverter());
		when(this.mockedNamedCache.entrySet()).thenThrow(new IllegalStateException("Unexpected call to entrySet"));
		when(this.mockedContinuousQueryCache.entrySet()).thenThrow(new IllegalStateException("Unexpected call to entrySet"));
	}

	@Test
	public void testNamedCacheConversion() {
		final Map map = this.conversionService.convert(this.mockedNamedCache, Map.class);
		assertThat(map).isSameAs(this.mockedNamedCache);

		final NamedCache namedCache = this.conversionService.convert(this.mockedNamedCache, NamedCache.class);
		assertThat(namedCache).isSameAs(this.mockedNamedCache);

		final NamedMap namedMap = this.conversionService.convert(this.mockedNamedCache, NamedMap.class);
		assertThat(namedMap).isSameAs(this.mockedNamedCache);

		final CacheMap cacheMap = this.conversionService.convert(this.mockedNamedCache, CacheMap.class);
		assertThat(cacheMap).isSameAs(this.mockedNamedCache);

		final NamedCollection namedCollection = this.conversionService.convert(this.mockedNamedCache, NamedCollection.class);
		assertThat(namedCollection).isSameAs(this.mockedNamedCache);

		final ObservableMap observableMap = this.conversionService.convert(this.mockedNamedCache, ObservableMap.class);
		assertThat(observableMap).isSameAs(this.mockedNamedCache);

		final ConcurrentMap concurrentMap = this.conversionService.convert(this.mockedNamedCache, ConcurrentMap.class);
		assertThat(concurrentMap).isSameAs(this.mockedNamedCache);

		final QueryMap queryMap = this.conversionService.convert(this.mockedNamedCache, QueryMap.class);
		assertThat(queryMap).isSameAs(this.mockedNamedCache);

		final InvocableMap invocableMap = this.conversionService.convert(this.mockedNamedCache, InvocableMap.class);
		assertThat(invocableMap).isSameAs(this.mockedNamedCache);

		final Releasable releasable = this.conversionService.convert(this.mockedNamedCache, Releasable.class);
		assertThat(releasable).isSameAs(this.mockedNamedCache);
	}

	@Test
	public void testContinuousQueryCacheConversion() {
		final Map map = this.conversionService.convert(this.mockedContinuousQueryCache, Map.class);
		assertThat(map).isSameAs(this.mockedContinuousQueryCache);

		final ContinuousQueryCache continuousQueryCache = this.conversionService.convert(this.mockedContinuousQueryCache, ContinuousQueryCache.class);
		assertThat(continuousQueryCache).isSameAs(this.mockedContinuousQueryCache);

		final AbstractKeySetBasedMap abstractKeySetBasedMap = this.conversionService.convert(this.mockedContinuousQueryCache, AbstractKeySetBasedMap.class);
		assertThat(abstractKeySetBasedMap).isSameAs(this.mockedContinuousQueryCache);

		final AbstractKeyBasedMap abstractKeyBasedMap = this.conversionService.convert(this.mockedContinuousQueryCache, AbstractKeyBasedMap.class);
		assertThat(abstractKeyBasedMap).isSameAs(this.mockedContinuousQueryCache);

		final NamedCache namedCache = this.conversionService.convert(this.mockedContinuousQueryCache, NamedCache.class);
		assertThat(namedCache).isSameAs(this.mockedContinuousQueryCache);

		final NamedMap namedMap = this.conversionService.convert(this.mockedContinuousQueryCache, NamedMap.class);
		assertThat(namedMap).isSameAs(this.mockedContinuousQueryCache);

		final CacheMap cacheMap = this.conversionService.convert(this.mockedContinuousQueryCache, CacheMap.class);
		assertThat(cacheMap).isSameAs(this.mockedContinuousQueryCache);

		final NamedCollection namedCollection = this.conversionService.convert(this.mockedContinuousQueryCache, NamedCollection.class);
		assertThat(namedCollection).isSameAs(this.mockedContinuousQueryCache);

		final ObservableMap observableMap = this.conversionService.convert(this.mockedContinuousQueryCache, ObservableMap.class);
		assertThat(observableMap).isSameAs(this.mockedContinuousQueryCache);

		final ConcurrentMap concurrentMap = this.conversionService.convert(this.mockedContinuousQueryCache, ConcurrentMap.class);
		assertThat(concurrentMap).isSameAs(this.mockedContinuousQueryCache);

		final QueryMap queryMap = this.conversionService.convert(this.mockedContinuousQueryCache, QueryMap.class);
		assertThat(queryMap).isSameAs(this.mockedContinuousQueryCache);

		final InvocableMap invocableMap = this.conversionService.convert(this.mockedContinuousQueryCache, InvocableMap.class);
		assertThat(invocableMap).isSameAs(this.mockedContinuousQueryCache);

		final Releasable releasable = this.conversionService.convert(this.mockedContinuousQueryCache, Releasable.class);
		assertThat(releasable).isSameAs(this.mockedContinuousQueryCache);
	}
}
