/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.annotation.Resource;
import javax.inject.Inject;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.AsyncNamedCache;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.QueryMap;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.oracle.coherence.spring.configuration.NamedCacheConfiguration.COHERENCE_CACHE_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig(CoherenceNamedCacheConfigurationTests.Config.class)
@DirtiesContext
public class CoherenceNamedCacheConfigurationTests {

	@Autowired
	@Name("fooCache")
	private NamedCache fooCache;

	@Test
	@Order(1)
	public void getDefaultSession() throws Exception {
		assertThat(this.fooCache).hasSize(0);
	}

	@Inject
	ApplicationContext ctx;

	@Test
	void shouldInjectAsyncNamedCacheUsingFieldName() {
		AsyncNamedCacheFieldsBean bean = this.ctx.getBean(AsyncNamedCacheFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getNamedCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectAsyncNamedCacheWithGenericKeys() {
		AsyncNamedCacheFieldsBean bean = this.ctx.getBean(AsyncNamedCacheFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getNamedCache().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectAsyncNamedCacheWithGenericValues() {
		AsyncNamedCacheFieldsBean bean = this.ctx.getBean(AsyncNamedCacheFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getNamedCache().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectAsyncNamedCacheWithGenerics() {
		AsyncNamedCacheFieldsBean bean = this.ctx.getBean(AsyncNamedCacheFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getNamedCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectCachesFromDifferentSessions() throws ExecutionException, InterruptedException {
		DifferentSessionBean bean = this.ctx.getBean(DifferentSessionBean.class);

		assertThat(bean.getDefaultCcfNumbers(), is(notNullValue()));
		assertThat(bean.getDefaultCcfNumbers().getName(), is("numbers"));
		assertThat(bean.getDefaultCcfAsyncNumbers(), is(notNullValue()));
		assertThat(bean.getDefaultCcfAsyncNumbers().getNamedCache().getName(), is("numbers"));

		bean.getDefaultCcfAsyncNumbers().put("foo", 1234);
		assertThat(bean.getDefaultCcfAsyncNumbers(), is(notNullValue()));

		assertThat(bean.getDefaultCcfAsyncNumbers().size().get(), is(1));
		assertThat(bean.getDefaultCcfNumbers().size(), is(1));

		assertThat(bean.getDefaultCcfAsyncNumbers().get("foo").get(), is(1234));
		assertThat(bean.getDefaultCcfNumbers().get("foo"), is(1234));

		assertThat(bean.getSpecificCcfNumbers(), is(notNullValue()));
		assertThat(bean.getSpecificCcfNumbers().getName(), is("numbers"));
		assertThat(bean.getSpecificCcfAsyncNumbers(), is(notNullValue()));
		assertThat(bean.getSpecificCcfAsyncNumbers().getNamedCache().getName(), is("numbers"));

		bean.getSpecificCcfAsyncNumbers().put("foo", 1234);

		assertThat(bean.getSpecificCcfAsyncNumbers().size().get(), is(1));
		assertThat(bean.getSpecificCcfNumbers().size(), is(1));

		assertThat(bean.getSpecificCcfAsyncNumbers().get("foo").get(), is(1234));
		assertThat(bean.getSpecificCcfNumbers().get("foo"), is(1234));

		assertThat(bean.getDefaultCcfNumbers(), is(not(bean.getSpecificCcfNumbers())));
	}

	@Test
	void shouldInjectNamedCacheUsingFieldName() {
		NamedCacheFieldsBean bean = this.ctx.getBean(NamedCacheFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getName(), is("numbers"));
	}

	@Test
	void shouldInjectNamedCacheWithGenericKeys() {
		NamedCacheFieldsBean bean = this.ctx.getBean(NamedCacheFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys().getName(), is("genericKeys"));
	}

	@Test
	void shouldInjectNamedCacheWithGenericValues() {
		NamedCacheFieldsBean bean = this.ctx.getBean(NamedCacheFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getName(), is("genericValues"));
	}

	@Test
	void shouldInjectNamedCacheWithGenerics() {
		NamedCacheFieldsBean bean = this.ctx.getBean(NamedCacheFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedAsyncNamedCache() {
		AsyncNamedCacheFieldsBean bean = this.ctx.getBean(AsyncNamedCacheFieldsBean.class);
		assertThat(bean.getNamedCache(), is(notNullValue()));
		assertThat(bean.getNamedCache().getNamedCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedNamedCache() {
		NamedCacheFieldsBean bean = this.ctx.getBean(NamedCacheFieldsBean.class);
		assertThat(bean.getNamedCache(), is(notNullValue()));
		assertThat(bean.getNamedCache().getName(), is("numbers"));
	}

	@Test
	void shouldInjectSuperTypeCacheMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		CacheMap map = bean.getCacheMap();
		NamedCache cache = bean.getNamedCache();
		assertThat(map, is(notNullValue()));
		assertThat(cache, is(notNullValue()));
		assertThat(map, is(sameInstance(cache)));
	}

	@Test
	void shouldInjectSuperTypeConcurrentMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		ConcurrentMap map = bean.getConcurrentMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedCache())));
	}

	@Test
	void shouldInjectSuperTypeInvocableMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		InvocableMap map = bean.getInvocableMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedCache())));
	}

	@Test
	void shouldInjectSuperTypeObservableMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		ObservableMap map = bean.getObservableMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedCache())));
	}

	@Test
	void shouldInjectSuperTypeQueryMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		QueryMap map = bean.getQueryMap();
		assertThat(map, is(notNullValue()));
		assertThat(map, is(sameInstance(bean.getNamedCache())));
	}

	@Test
	void testCtorInjection() {
		CtorBean bean = this.ctx.getBean(CtorBean.class);

		assertThat(bean.getNumbers(), notNullValue());
		assertThat(bean.getNumbers().getName(), is("numbers"));
		assertThat(bean.getLetters(), notNullValue());
		assertThat(bean.getLetters().getNamedCache().getName(), is("letters"));
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
		NamedCacheFieldsBean namedCacheFieldsBean() {
			return new NamedCacheFieldsBean();
		}

		@Bean
		SuperTypesBean superTypesBean() {
			return new SuperTypesBean();
		}

		@Bean
		AsyncNamedCacheFieldsBean asyncNamedCacheFieldsBean() {
			return new AsyncNamedCacheFieldsBean();
		}

		@Bean
		DifferentSessionBean differentSessionBean() {
			return new DifferentSessionBean();
		}

		/**
		 * Constructor Injection will not work with {@link NamedCache} as it implements the {@link java.util.Map}
		 * interface.
		 * @param letters Injects a AsyncNamedCache
		 * @return new CtorBean
		 */
		@Bean
		CtorBean ctorBean(
				@Name("letters") AsyncNamedCache<String, String> letters) {
			return new CtorBean(letters);
		}
	}

	static class NamedCacheFieldsBean {
		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		private NamedCache numbers;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private NamedCache namedCache;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private NamedCache<Integer, String> genericCache;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		private NamedCache<List<String>, String> genericKeys;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		private NamedCache<String, List<String>> genericValues;

		NamedCache<Integer, String> getGenericCache() {
			return this.genericCache;
		}

		NamedCache<List<String>, String> getGenericKeys() {
			return this.genericKeys;
		}

		NamedCache<String, List<String>> getGenericValues() {
			return this.genericValues;
		}

		NamedCache getNamedCache() {
			return this.namedCache;
		}

		NamedCache getNumbers() {
			return this.numbers;
		}
	}

	static class AsyncNamedCacheFieldsBean {
		@Inject
		private AsyncNamedCache numbers;

		@Inject
		@Name("numbers")
		private AsyncNamedCache namedCache;

		@Inject
		@Name("numbers")
		private AsyncNamedCache<Integer, String> genericCache;

		@Inject
		private AsyncNamedCache<List<String>, String> genericKeys;

		@Inject
		private AsyncNamedCache<String, List<String>> genericValues;

		AsyncNamedCache<Integer, String> getGenericCache() {
			return this.genericCache;
		}

		AsyncNamedCache<List<String>, String> getGenericKeys() {
			return this.genericKeys;
		}

		AsyncNamedCache<String, List<String>> getGenericValues() {
			return this.genericValues;
		}

		AsyncNamedCache getNamedCache() {
			return this.namedCache;
		}

		AsyncNamedCache getNumbers() {
			return this.numbers;
		}
	}

	static class DifferentSessionBean {
		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private NamedCache defaultCcfNumbers;

		@Inject
		@Name("numbers")
		private AsyncNamedCache defaultCcfAsyncNumbers;

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		@SessionName("test")
		private NamedCache specificCcfNumbers;

		@Inject
		@Name("numbers")
		@SessionName("test")
		private AsyncNamedCache specificCcfAsyncNumbers;

		AsyncNamedCache getDefaultCcfAsyncNumbers() {
			return this.defaultCcfAsyncNumbers;
		}

		NamedCache getDefaultCcfNumbers() {
			return this.defaultCcfNumbers;
		}

		AsyncNamedCache getSpecificCcfAsyncNumbers() {
			return this.specificCcfAsyncNumbers;
		}

		NamedCache getSpecificCcfNumbers() {
			return this.specificCcfNumbers;
		}
	}

	static class CtorBean {

		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private NamedCache<Integer, String> numbers;

		private final AsyncNamedCache<String, String> letters;

		CtorBean(AsyncNamedCache<String, String> letters) {
			this.letters = letters;
		}

		AsyncNamedCache<String, String> getLetters() {
			return this.letters;
		}

		NamedCache<Integer, String> getNumbers() {
			return this.numbers;
		}
	}

	static class SuperTypesBean {
		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("numbers")
		private NamedCache<Integer, String> namedCache;

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

		NamedCache<Integer, String> getNamedCache() {
			return this.namedCache;
		}

		ObservableMap<Integer, String> getObservableMap() {
			return this.observableMap;
		}

		QueryMap<Integer, String> getQueryMap() {
			return this.queryMap;
		}
	}
}
