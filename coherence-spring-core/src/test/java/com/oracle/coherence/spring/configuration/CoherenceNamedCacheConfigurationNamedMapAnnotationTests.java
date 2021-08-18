/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.List;

import com.oracle.coherence.spring.configuration.annotation.CoherenceAsyncMap;
import com.oracle.coherence.spring.configuration.annotation.CoherenceCache;
import com.oracle.coherence.spring.configuration.annotation.CoherenceMap;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.AsyncNamedMap;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
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
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.Config.class)
@DirtiesContext
class CoherenceNamedCacheConfigurationNamedMapAnnotationTests {

	@Autowired
	ApplicationContext ctx;

	@Autowired
	Coherence coherence;

	@Test
	void shouldInjectAsyncNamedMapUsingFieldName() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenericKeys() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getNamedMap().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenericValues() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getNamedMap().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenerics() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectCachesFromDifferentSessions() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.DifferentSessionBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.DifferentSessionBean.class);

		assertThat(bean.getDefaultCcfNumbers(), is(notNullValue()));
		assertThat(bean.getDefaultCcfNumbers().getName(), is("numbers"));
		assertThat(bean.getDefaultCcfAsyncNumbers(), is(notNullValue()));
		assertThat(bean.getDefaultCcfAsyncNumbers().getNamedMap().getName(), is("numbers"));

		assertThat(bean.getSpecificCcfNumbers(), is(notNullValue()));
		assertThat(bean.getSpecificCcfNumbers().getName(), is("numbers"));
		assertThat(bean.getSpecificCcfAsyncNumbers(), is(notNullValue()));
		assertThat(bean.getSpecificCcfAsyncNumbers().getNamedMap().getName(), is("numbers"));

		assertThat(bean.getDefaultCcfNumbers(), is(not(bean.getSpecificCcfNumbers())));
	}

	@Test
	void shouldInjectNamedMapUsingFieldName() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getName(), is("numbers"));
	}

	@Test
	void shouldInjectNamedMapWithGenericKeys() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectNamedMapWithGenericValues() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectNamedMapWithGenerics() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedAsyncNamedMap() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean.class);
		assertThat(bean.getNamedMap(), is(notNullValue()));
		assertThat(bean.getNamedMap().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedNamedMap() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean.class);
		assertThat(bean.getNamedMap(), is(notNullValue()));
		assertThat(bean.getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectSuperTypeCacheMap() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean.class);
		CacheMap map = bean.getCacheMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	void shouldInjectSuperTypeConcurrentMap() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean.class);
		ConcurrentMap map = bean.getConcurrentMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	void shouldInjectSuperTypeInvocableMap() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean.class);
		InvocableMap map = bean.getInvocableMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	void shouldInjectSuperTypeObservableMap() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean.class);
		ObservableMap map = bean.getObservableMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	void shouldInjectSuperTypeQueryMap() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean.class);
		QueryMap map = bean.getQueryMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	void testCtorInjection() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.CtorBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.CtorBean.class);

		assertThat(bean.getNumbers(), notNullValue());
		assertThat(bean.getNumbers().getName(), is("numbers"));
		assertThat(bean.getLetters(), notNullValue());
		assertThat(bean.getLetters().getNamedMap().getName(), is("letters"));
	}

	@Test
	void testAnotherConstructorBeanInjection() {
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AnotherConstructorBean bean = this.ctx.getBean(CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AnotherConstructorBean.class);

		assertThat(bean.getNumbers(), notNullValue());
		assertThat(bean.getNumbers().getName(), is("numbers"));
		assertThat(bean.getLetters(), notNullValue());
		assertThat(bean.getLetters().getName(), is("letters"));

		bean.getNumbers().put(1, "foo");
		assertThat(this.coherence.getSession("test").getMap("numbers").size(), is(1));
		assertThat(this.coherence.getSession().getMap("numbers").size(), is(0));
	}
	// ----- test beans -----------------------------------------------------

	static class NamedMapFieldsBean {
		@CoherenceCache
		private NamedMap numbers;

		@CoherenceCache("numbers")
		private NamedMap namedMap;

		@CoherenceCache("numbers")
		private NamedMap<Integer, String> genericCache;

		@CoherenceCache
		private NamedMap<List<String>, String> genericKeys;

		@CoherenceCache
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
		@CoherenceAsyncMap
		private AsyncNamedMap numbers;

		@CoherenceAsyncMap("numbers")
		private AsyncNamedMap namedMap;

		@CoherenceAsyncMap("numbers")
		private AsyncNamedMap<Integer, String> genericCache;

		@CoherenceAsyncMap
		private AsyncNamedMap<List<String>, String> genericKeys;

		@CoherenceAsyncMap
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
		@CoherenceCache("numbers")
		private NamedMap defaultCcfNumbers;

		@CoherenceAsyncMap("numbers")
		private AsyncNamedMap defaultCcfAsyncNumbers;

		@CoherenceCache(name = "numbers", session = "test")
		private NamedMap specificCcfNumbers;

		@CoherenceAsyncMap(name = "numbers", session = "test")
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

	static class AnotherConstructorBean {

		private NamedMap<Integer, String> numbers;

		private final NamedCache<String, String> letters;

		AnotherConstructorBean(
				NamedMap<Integer, String> numbers,
				NamedCache<String, String> letters) {

			this.numbers = numbers;
			this.letters = letters;
		}

		NamedCache<String, String> getLetters() {
			return this.letters;
		}

		NamedMap<Integer, String> getNumbers() {
			return this.numbers;
		}
	}

	static class SuperTypesBean {
		@CoherenceCache("numbers")
		private NamedMap<Integer, String> namedMap;

		@CoherenceCache("numbers")
		private InvocableMap<Integer, String> invocableMap;

		@CoherenceCache("numbers")
		private ObservableMap<Integer, String> observableMap;

		@CoherenceCache("numbers")
		private ConcurrentMap<Integer, String> concurrentMap;

		@CoherenceCache("numbers")
		private QueryMap<Integer, String> queryMap;

		@CoherenceCache("numbers")
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
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean namedMapFieldsBean() {
			return new CoherenceNamedCacheConfigurationNamedMapAnnotationTests.NamedMapFieldsBean();
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean asyncNamedMapFieldsBean() {
			return new CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AsyncNamedMapFieldsBean();
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.DifferentSessionBean differentSessionBean() {
			return new CoherenceNamedCacheConfigurationNamedMapAnnotationTests.DifferentSessionBean();
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.CtorBean ctorBean(
				@CoherenceCache("numbers") NamedMap<Integer, String> numbers,
				@CoherenceAsyncMap("letters") AsyncNamedMap<String, String> letters) {
			return new CoherenceNamedCacheConfigurationNamedMapAnnotationTests.CtorBean(numbers, letters);
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean superTypesBean() {
			return new CoherenceNamedCacheConfigurationNamedMapAnnotationTests.SuperTypesBean();
		}

		@Bean
		CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AnotherConstructorBean anotherConstructorBean(
				@CoherenceMap(name = "numbers", session = "test") NamedMap<Integer, String> numbers,
				@CoherenceMap(name = "letters") NamedCache<String, String> letters) {
			return new CoherenceNamedCacheConfigurationNamedMapAnnotationTests.AnotherConstructorBean(numbers, letters);
		}

	}
}
