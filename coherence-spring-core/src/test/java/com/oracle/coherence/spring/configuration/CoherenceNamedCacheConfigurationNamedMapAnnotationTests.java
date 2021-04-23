/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.List;

import com.oracle.coherence.spring.configuration.annotation.AsyncNamedCache;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.annotation.NamedCache;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.AsyncNamedMap;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.QueryMap;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.Config.class)
@DirtiesContext
class CoherenceNamedCacheConfigurationNamedMapAnnotationTests {

	@Autowired
	ApplicationContext ctx;

	@Test
	void shouldInjectAsyncNamedMapUsingFieldName() {
		CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenericKeys() {
		CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getNamedMap().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenericValues() {
		CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getNamedMap().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenerics() {
		CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectCachesFromDifferentSessions() {
		CoherenceNamedCacheConfigurationNamedMapTests.DifferentSessionBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.DifferentSessionBean.class);

		assertThat(bean.getDefaultCcfNumbers(), is(notNullValue()));
		assertThat(bean.getDefaultCcfNumbers().getName(), is("numbers"));
		assertThat(bean.getDefaultCcfAsyncNumbers(), is(notNullValue()));
		assertThat(bean.getDefaultCcfAsyncNumbers().getNamedMap().getName(), is("numbers"));
		assertThat(bean.getDefaultCcfAsyncNumbers().getNamedMap(), is(bean.getDefaultCcfNumbers()));

		assertThat(bean.getSpecificCcfNumbers(), is(notNullValue()));
		assertThat(bean.getSpecificCcfNumbers().getName(), is("numbers"));
		assertThat(bean.getSpecificCcfAsyncNumbers(), is(notNullValue()));
		assertThat(bean.getSpecificCcfAsyncNumbers().getNamedMap().getName(), is("numbers"));
		assertThat(bean.getSpecificCcfAsyncNumbers().getNamedMap(), is(bean.getSpecificCcfNumbers()));

		assertThat(bean.getDefaultCcfNumbers(), is(not(bean.getSpecificCcfNumbers())));
	}

	@Test
	void shouldInjectNamedMapUsingFieldName() {
		CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getName(), is("numbers"));
	}

	@Test
	void shouldInjectNamedMapWithGenericKeys() {
		CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectNamedMapWithGenericValues() {
		CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectNamedMapWithGenerics() {
		CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedAsyncNamedMap() {
		CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getNamedMap(), is(notNullValue()));
		assertThat(bean.getNamedMap().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedNamedMap() {
		CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean.class);
		assertThat(bean.getNamedMap(), is(notNullValue()));
		assertThat(bean.getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectSuperTypeCacheMap() {
		CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean.class);
		CacheMap map = bean.getCacheMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeConcurrentMap() {
		CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean.class);
		ConcurrentMap map = bean.getConcurrentMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeInvocableMap() {
		CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean.class);
		InvocableMap map = bean.getInvocableMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeObservableMap() {
		CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean.class);
		ObservableMap map = bean.getObservableMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeQueryMap() {
		CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean.class);
		QueryMap map = bean.getQueryMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void testCtorInjection() {
		CoherenceNamedCacheConfigurationNamedMapTests.CtorBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapTests.CtorBean.class);

		assertThat(bean.getNumbers(), notNullValue());
		assertThat(bean.getNumbers().getName(), is("numbers"));
		assertThat(bean.getLetters(), notNullValue());
		assertThat(bean.getLetters().getNamedMap().getName(), is("letters"));
	}

	// ----- test beans -----------------------------------------------------

	static class NamedMapFieldsBean {
		@NamedCache
		private NamedMap numbers;

		@NamedCache("numbers")
		private NamedMap namedMap;

		@NamedCache("numbers")
		private NamedMap<Integer, String> genericCache;

		@NamedCache
		private NamedMap<List<String>, String> genericKeys;

		@NamedCache
		private NamedMap<String, List<String>> genericValues;

		NamedMap<Integer, String> getGenericCache() {
			return this.genericCache;
		}

		NamedMap<List<String>, String> getGenericKeys() {
			return this.genericKeys;
		}

		NamedMap<String, List<String>> getGenericValues() {
			return this.genericValues;
		}

		NamedMap getNamedMap() {
			return this.namedMap;
		}

		NamedMap getNumbers() {
			return this.numbers;
		}
	}

	static class AsyncNamedMapFieldsBean {
		@AsyncNamedCache
		private AsyncNamedMap numbers;

		@AsyncNamedCache("numbers")
		private AsyncNamedMap namedMap;

		@AsyncNamedCache("numbers")
		private AsyncNamedMap<Integer, String> genericCache;

		@AsyncNamedCache
		private AsyncNamedMap<List<String>, String> genericKeys;

		@AsyncNamedCache
		private AsyncNamedMap<String, List<String>> genericValues;

		AsyncNamedMap<Integer, String> getGenericCache() {
			return this.genericCache;
		}

		AsyncNamedMap<List<String>, String> getGenericKeys() {
			return this.genericKeys;
		}

		AsyncNamedMap<String, List<String>> getGenericValues() {
			return this.genericValues;
		}

		AsyncNamedMap getNamedMap() {
			return this.namedMap;
		}

		AsyncNamedMap getNumbers() {
			return this.numbers;
		}
	}

	static class DifferentSessionBean {
		@NamedCache("numbers")
		private NamedMap defaultCcfNumbers;

		@AsyncNamedCache("numbers")
		private AsyncNamedMap defaultCcfAsyncNumbers;

		@NamedCache(cacheName = "numbers", sessionName = "test")
		private NamedMap specificCcfNumbers;

		@AsyncNamedCache(cacheName = "numbers", sessionName = "test")
		private AsyncNamedMap specificCcfAsyncNumbers;

		AsyncNamedMap getDefaultCcfAsyncNumbers() {
			return this.defaultCcfAsyncNumbers;
		}

		NamedMap getDefaultCcfNumbers() {
			return this.defaultCcfNumbers;
		}

		AsyncNamedMap getSpecificCcfAsyncNumbers() {
			return this.specificCcfAsyncNumbers;
		}

		NamedMap getSpecificCcfNumbers() {
			return this.specificCcfNumbers;
		}
	}

	static class CtorBean {

		private NamedMap<Integer, String> numbers;

		private final AsyncNamedMap<String, String> letters;

		CtorBean(NamedMap<Integer, String> numbers,
		         AsyncNamedMap<String, String> letters) {

			this.numbers = numbers;
			this.letters = letters;
		}

		AsyncNamedMap<String, String> getLetters() {
			return this.letters;
		}

		NamedMap<Integer, String> getNumbers() {
			return this.numbers;
		}
	}

	static class SuperTypesBean {
		@NamedCache("numbers")
		private NamedMap<Integer, String> namedMap;

		@NamedCache("numbers")
		private InvocableMap<Integer, String> invocableMap;

		@NamedCache("numbers")
		private ObservableMap<Integer, String> observableMap;

		@NamedCache("numbers")
		private ConcurrentMap<Integer, String> concurrentMap;

		@NamedCache("numbers")
		private QueryMap<Integer, String> queryMap;

		@NamedCache("numbers")
		private CacheMap<Integer, String> cacheMap;

		CacheMap<Integer, String> getCacheMap() {
			return this.cacheMap;
		}

		ConcurrentMap<Integer, String> getConcurrentMap() {
			return this.concurrentMap;
		}

		InvocableMap<Integer, String> getInvocableMap() {
			return this.invocableMap;
		}

		NamedMap<Integer, String> getNamedMap() {
			return this.namedMap;
		}

		ObservableMap<Integer, String> getObservableMap() {
			return this.observableMap;
		}

		QueryMap<Integer, String> getQueryMap() {
			return this.queryMap;
		}
	}

	@Configuration
	@EnableCoherence
	static class Config {
		@Bean
		SessionConfigurationBean testSessionConfigurationBean() {
			final SessionConfigurationBean sessionConfigurationBean = new SessionConfigurationBean();
			sessionConfigurationBean.setName("test");
			sessionConfigurationBean.setConfig("test-coherence-config.xml");
			sessionConfigurationBean.setType(SessionType.SERVER);
			sessionConfigurationBean.setScopeName("Test");
			return sessionConfigurationBean;
		}

		@Bean
		SessionConfigurationBean defaultSessionConfigurationBean() {
			final SessionConfigurationBean sessionConfigurationBean = new SessionConfigurationBean();
			sessionConfigurationBean.setName(Coherence.DEFAULT_NAME);
			sessionConfigurationBean.setConfig("coherence-cache-config.xml");
			sessionConfigurationBean.setType(SessionType.SERVER);
			return sessionConfigurationBean;
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean namedMapFieldsBean() {
			return new CoherenceNamedCacheConfigurationNamedMapTests.NamedMapFieldsBean();
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean asyncNamedMapFieldsBean() {
			return new CoherenceNamedCacheConfigurationNamedMapTests.AsyncNamedMapFieldsBean();
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapTests.DifferentSessionBean differentSessionBean() {
			return new CoherenceNamedCacheConfigurationNamedMapTests.DifferentSessionBean();
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapTests.CtorBean ctorBean(
				@NamedCache("numbers") NamedMap<Integer, String> numbers,
				@AsyncNamedCache("letters") AsyncNamedMap<String, String> letters) {
			return new CoherenceNamedCacheConfigurationNamedMapTests.CtorBean(numbers, letters);
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean superTypesBean() {
			return new CoherenceNamedCacheConfigurationNamedMapTests.SuperTypesBean();
		}

	}
}
