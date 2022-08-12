/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.oracle.coherence.spring.configuration.NamedCacheConfiguration.COHERENCE_CACHE_BEAN_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig(CoherenceNamedCacheConfigurationNamedMapTests.Config.class)
@DirtiesContext
class CoherenceNamedCacheConfigurationNamedMapTests {

	@Inject
	ApplicationContext ctx;

	@Test
	void shouldInjectAsyncNamedMapUsingFieldName() {
		AsyncNamedMapFieldsBean bean = this.ctx.getBean(AsyncNamedMapFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenericKeys() {
		AsyncNamedMapFieldsBean bean = this.ctx.getBean(AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getNamedMap().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenericValues() {
		AsyncNamedMapFieldsBean bean = this.ctx.getBean(AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getNamedMap().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectAsyncNamedMapWithGenerics() {
		AsyncNamedMapFieldsBean bean = this.ctx.getBean(AsyncNamedMapFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectCachesFromDifferentSessions() {
		DifferentSessionBean bean = this.ctx.getBean(DifferentSessionBean.class);

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
		NamedMapFieldsBean bean = this.ctx.getBean(NamedMapFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getName(), is("numbers"));
	}

	@Test
	void shouldInjectNamedMapWithGenericKeys() {
		NamedMapFieldsBean bean = this.ctx.getBean(NamedMapFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectNamedMapWithGenericValues() {
		NamedMapFieldsBean bean = this.ctx.getBean(NamedMapFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectNamedMapWithGenerics() {
		NamedMapFieldsBean bean = this.ctx.getBean(NamedMapFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedAsyncNamedMap() {
		AsyncNamedMapFieldsBean bean = this.ctx.getBean(AsyncNamedMapFieldsBean.class);
		assertThat(bean.getNamedMap(), is(notNullValue()));
		assertThat(bean.getNamedMap().getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedNamedMap() {
		NamedMapFieldsBean bean = this.ctx.getBean(NamedMapFieldsBean.class);
		assertThat(bean.getNamedMap(), is(notNullValue()));
		assertThat(bean.getNamedMap().getName(), is("numbers"));
	}

	@Test
	void shouldInjectSuperTypeCacheMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		CacheMap map = bean.getCacheMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeConcurrentMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		ConcurrentMap map = bean.getConcurrentMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeInvocableMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		InvocableMap map = bean.getInvocableMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeObservableMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		ObservableMap map = bean.getObservableMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void shouldInjectSuperTypeQueryMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		QueryMap map = bean.getQueryMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedMap())));
	}

	@Test
	void testCtorInjection() {
		CtorBean bean = this.ctx.getBean(CtorBean.class);

		assertThat(bean.getNumbers(), notNullValue());
		assertThat(bean.getNumbers().getName(), is("numbers"));
		assertThat(bean.getLetters(), notNullValue());
		assertThat(bean.getLetters().getNamedMap().getName(), is("letters"));
	}

	// ----- test beans -----------------------------------------------------

	static class NamedMapFieldsBean {
		@Inject
		private NamedMap numbers;

		@Inject
		@Name("numbers")
		private NamedMap namedMap;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private NamedMap<Integer, String> genericCache;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		private NamedMap<List<String>, String> genericKeys;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
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
		@Inject
		private AsyncNamedMap numbers;

		@Inject
		@Name("numbers")
		private AsyncNamedMap namedMap;

		@Inject
		@Name("numbers")
		private AsyncNamedMap<Integer, String> genericCache;

		@Inject
		private AsyncNamedMap<List<String>, String> genericKeys;

		@Inject
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
		@Inject
		@Name("numbers")
		private NamedMap defaultCcfNumbers;

		@Inject
		@Name("numbers")
		private AsyncNamedMap defaultCcfAsyncNumbers;

		@Inject
		@Name("numbers")
		@SessionName("test")
		private NamedMap specificCcfNumbers;

		@Inject
		@Name("numbers")
		@SessionName("test")
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
		@Value("#{getCache}")
		@Name("numbers")
		private NamedMap<Integer, String> namedMap;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private InvocableMap<Integer, String> invocableMap;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private ObservableMap<Integer, String> observableMap;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private ConcurrentMap<Integer, String> concurrentMap;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private QueryMap<Integer, String> queryMap;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
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
		NamedMapFieldsBean namedMapFieldsBean() {
			return new NamedMapFieldsBean();
		}

		@Bean
		AsyncNamedMapFieldsBean asyncNamedMapFieldsBean() {
			return new AsyncNamedMapFieldsBean();
		}

		@Bean
		DifferentSessionBean differentSessionBean() {
			return new DifferentSessionBean();
		}

		@Bean
		CtorBean ctorBean(
				@Value("#{getCache}") @Name("numbers") NamedMap<Integer, String> numbers,
				@Name("letters") AsyncNamedMap<String, String> letters) {
			return new CtorBean(numbers, letters);
		}

		@Bean
		SuperTypesBean superTypesBean() {
			return new SuperTypesBean();
		}

	}
}
