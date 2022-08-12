/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;

import com.oracle.coherence.spring.annotation.AlwaysFilter;
import com.oracle.coherence.spring.annotation.ChainedExtractor;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.View;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.configuration.annotation.CoherenceCache;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.net.cache.ContinuousQueryCache;
import com.tangosol.util.ConcurrentMap;
import com.tangosol.util.Filters;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ObservableMap;
import com.tangosol.util.QueryMap;
import data.Person;
import data.PhoneNumber;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.oracle.coherence.spring.configuration.NamedCacheConfiguration.COHERENCE_CACHE_BEAN_NAME;
import static com.oracle.coherence.spring.configuration.NamedCacheConfiguration.COHERENCE_VIEW_BEAN_NAME;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig(CoherenceNamedCacheConfigurationViewTest.Config.class)
@DirtiesContext
class CoherenceNamedCacheConfigurationViewTest {

	@Inject
	ApplicationContext ctx;

	@Test
	void shouldInjectContinuousQueryCacheUsingFieldName() {
		ContinuousQueryCacheFieldsBean bean = this.ctx.getBean(ContinuousQueryCacheFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getNumbers().getCache().getCacheName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedNamedCache() {
		ContinuousQueryCacheFieldsBean bean = this.ctx.getBean(ContinuousQueryCacheFieldsBean.class);
		assertThat(bean.getNamedCache(), is(notNullValue()));
		assertThat(bean.getNamedCache(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getNamedCache().getCache().getCacheName(), is("numbers"));
	}

	@Test
	void shouldInjectContinuousQueryCacheWithGenerics() {
		ContinuousQueryCacheFieldsBean bean = this.ctx.getBean(ContinuousQueryCacheFieldsBean.class);
		assertThat(bean.getGenericCache(), is(notNullValue()));
		assertThat(bean.getGenericCache(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getGenericCache().getCache().getCacheName(), is("numbers"));
	}

	@Test
	void shouldInjectContinuousQueryCacheWithGenericKeys() {
		ContinuousQueryCacheFieldsBean bean = this.ctx.getBean(ContinuousQueryCacheFieldsBean.class);
		assertThat(bean.getGenericKeys(), is(notNullValue()));
		assertThat(bean.getGenericKeys(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getGenericKeys().getCache().getCacheName(), is("genericKeys"));
	}

	@Test
	void shouldInjectContinuousQueryCacheWithGenericValues() {
		ContinuousQueryCacheFieldsBean bean = this.ctx.getBean(ContinuousQueryCacheFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getGenericValues().getCache().getCacheName(), is("genericValues"));
	}

	@Test
	void shouldInjectCachesFromDifferentSessions() {
		DifferentSessionsBean bean = this.ctx.getBean(DifferentSessionsBean.class);

		assertThat(bean.getDefaultCcfNumbers(), is(notNullValue()));
		assertThat(bean.getDefaultCcfNumbers(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getDefaultCcfNumbers().getCache().getCacheName(), is("numbers"));

		assertThat(bean.getSpecificCcfNumbers(), is(notNullValue()));
		assertThat(bean.getSpecificCcfNumbers(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getSpecificCcfNumbers().getCache().getCacheName(), is("numbers"));

		assertThat(bean.getDefaultCcfNumbers().getCache().getCacheService(),
				is(not(bean.getSpecificCcfNumbers().getCache().getCacheService())));
	}

	@Test
	void testCtorInjection() {
		CtorBean bean = this.ctx.getBean(CtorBean.class);

		assertThat(bean.getNumbers(), notNullValue());
		assertThat(bean.getNumbers(), is(instanceOf(ContinuousQueryCache.class)));
		assertThat(bean.getNumbers().getCache().getCacheName(), is("numbers"));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void shouldInjectSuperTypeContinuousQueryCache() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		ContinuousQueryCache cache = bean.getContinuousQueryCache();
		assertThat(cache, is(notNullValue()));
		assertThat(cache, is(sameInstance(bean.getContinuousQueryCache())));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void shouldInjectSuperTypeNamedCache() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		NamedCache cache = bean.getNamedCache();
		assertThat(cache, is(notNullValue()));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void shouldInjectSuperTypeInvocableMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		InvocableMap map = bean.getInvocableMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void shouldInjectSuperTypeObservableMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		ObservableMap map = bean.getObservableMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void shouldInjectSuperTypeConcurrentMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		ConcurrentMap map = bean.getConcurrentMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void shouldInjectSuperTypeQueryMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		QueryMap map = bean.getQueryMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	@SuppressWarnings("rawtypes")
	void shouldInjectSuperTypeCacheMap() {
		SuperTypesBean bean = this.ctx.getBean(SuperTypesBean.class);
		CacheMap map = bean.getCacheMap();
		assertThat(map, is(notNullValue()));
	}

	@Test
	void shouldInjectContinuousQueryCacheWithFilters() {
		ContinuousQueryCacheWithFiltersBean withFilters = this.ctx.getBean(ContinuousQueryCacheWithFiltersBean.class);
		NamedCache<String, Person> cache = withFilters.getCache();
		//ContinuousQueryCache<String, Person, Person> always = withFilters.getAlways();
		ContinuousQueryCache<String, Person, Person> foo = withFilters.getFoo();

		// populate the underlying cache
		populate(cache);
		//assertThat(always.size(), is(cache.size()));

		Set<Map.Entry<String, Person>> entries = cache.entrySet(Filters.equal("lastName", "foo"));
		assertThat(foo.size(), is(entries.size()));
		for (Map.Entry<String, Person> entry : entries) {
			MatcherAssert.assertThat(foo.get(entry.getKey()), CoreMatchers.is(entry.getValue()));
		}
	}

	@Test
	void shouldInjectContinuousQueryCacheWithTransformer() {
		WithTransformersBean bean = this.ctx.getBean(WithTransformersBean.class);
		NamedCache<String, Person> cache = bean.getNamedCache();
		ContinuousQueryCache<String, Person, String> names = bean.getNames();

		// populate the underlying cache
		populate(cache);

		assertThat(names.size(), is(cache.size()));
		for (Map.Entry<String, Person> entry : cache.entrySet()) {
			final String key = entry.getKey();
			final Person person = entry.getValue();

			MatcherAssert.assertThat(names.get(key), CoreMatchers.is(person.getFirstName()));
		}
	}

	@Test
	void shouldInjectContinuousQueryCacheWithTransformerAndFilter() {
		WithTransformersBean bean = this.ctx.getBean(WithTransformersBean.class);
		NamedCache<String, Person> cache = bean.getNamedCache();
		ContinuousQueryCache<String, Person, String> filtered = bean.getFilteredNames();

		// populate the underlying cache
		populate(cache);

		Set<Map.Entry<String, Person>> entries = cache.entrySet(Filters.equal("lastName", "foo"));
		assertThat(filtered.size(), is(entries.size()));
		for (Map.Entry<String, Person> entry : entries) {
			MatcherAssert
					.assertThat(filtered.get(entry.getKey()), CoreMatchers.is(entry.getValue().getPhoneNumber().getNumber()));
		}
	}

	@Test
	void shouldInjectContinuousQueryCacheWithKeysOnly() {
		WithTransformersBean bean = this.ctx.getBean(WithTransformersBean.class);
		NamedCache<String, Person> cache = bean.getNamedCache();
		ContinuousQueryCache<String, Person, String> keysOnly = bean.getKeysOnly();

		// populate the underlying cache
		populate(cache);

		assertThat(keysOnly.size(), is(cache.size()));
		assertThat(keysOnly.isCacheValues(), is(false));
	}

	private void populate(NamedCache<String, Person> cache) {
		for (int i = 0; i < 100; i++) {
			String lastName = (i % 2 == 0) ? "foo" : "bar";
			Person bean = new Person(String.valueOf(i),
									lastName,
									LocalDate.now(),
									new PhoneNumber(44, "12345" + i));

			cache.put(lastName + "-" + i, bean);
		}
	}

	// ----- test beans -----------------------------------------------------

	@SuppressWarnings("rawtypes")
	static class ContinuousQueryCacheFieldsBean {
		@Inject
		private ContinuousQueryCache numbers;

		@Inject
		@Name("numbers")
		@View
		private ContinuousQueryCache namedCache;

		@Inject
		@Name("numbers")
		@View
		private ContinuousQueryCache<Integer, String, String> genericCache;

		@Inject
		@View
		private ContinuousQueryCache<List<String>, String, String> genericKeys;

		@Inject
		@View
		private ContinuousQueryCache<String, List<String>, String> genericValues;

		ContinuousQueryCache getNumbers() {
			return this.numbers;
		}

		ContinuousQueryCache getNamedCache() {
			return this.namedCache;
		}

		ContinuousQueryCache<Integer, String, String> getGenericCache() {
			return this.genericCache;
		}

		ContinuousQueryCache<List<String>, String, String> getGenericKeys() {
			return this.genericKeys;
		}

		ContinuousQueryCache<String, List<String>, String> getGenericValues() {
			return this.genericValues;
		}
	}

	static class ContinuousQueryCacheWithFiltersBean {
		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		private NamedCache<String, Person> beans;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@AlwaysFilter
		@Name("beans")
		@View
		private ContinuousQueryCache<String, Person, Person> always;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@WhereFilter("lastName = 'foo'")
		@Name("beans")
		@View
		private ContinuousQueryCache<String, Person, Person> foo;

		NamedCache<String, Person> getCache() {
			return this.beans;
		}

		ContinuousQueryCache<String, Person, Person> getAlways() {
			return this.always;
		}

		ContinuousQueryCache<String, Person, Person> getFoo() {
			return this.foo;
		}
	}

	@SuppressWarnings("rawtypes")
	static class DifferentSessionsBean {
		@Inject
		@Name("numbers")
		@View
		private ContinuousQueryCache defaultCcfNumbers;

		@Inject
		@Name("numbers")
		@View
		@SessionName("test")
		private ContinuousQueryCache specificCcfNumbers;

		ContinuousQueryCache getDefaultCcfNumbers() {
			return this.defaultCcfNumbers;
		}

		ContinuousQueryCache getSpecificCcfNumbers() {
			return this.specificCcfNumbers;
		}
	}

	static class CtorBean {

		private NamedCache<Integer, String> view;

		private final ContinuousQueryCache<Integer, String, String> numbers;

		CtorBean(NamedCache<Integer, String> view,
				ContinuousQueryCache<Integer, String, String> numbers) {
			this.view = view;
			this.numbers = numbers;
		}

		NamedCache<Integer, String> getView() {
			return this.view;
		}

		ContinuousQueryCache<Integer, String, String> getNumbers() {
			return this.numbers;
		}
	}

	static class SuperTypesBean {
		@Inject
		@Name("numbers")
		@View
		private ContinuousQueryCache<Integer, String, String> cqc;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@Name("numbers")
		@View
		private NamedCache<Integer, String> namedCache;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@Name("numbers")
		@View
		private InvocableMap<Integer, String> invocableMap;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@Name("numbers")
		@View
		private ObservableMap<Integer, String> observableMap;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@Name("numbers")
		@View
		private ConcurrentMap<Integer, String> concurrentMap;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@Name("numbers")
		@View
		private QueryMap<Integer, String> queryMap;

		@Resource(name = COHERENCE_VIEW_BEAN_NAME)
		@Name("numbers")
		@View
		private CacheMap<Integer, String> cacheMap;

		ContinuousQueryCache<Integer, String, String> getContinuousQueryCache() {
			return this.cqc;
		}

		NamedCache<Integer, String> getNamedCache() {
			return this.namedCache;
		}

		InvocableMap<Integer, String> getInvocableMap() {
			return this.invocableMap;
		}

		ObservableMap<Integer, String> getObservableMap() {
			return this.observableMap;
		}

		ConcurrentMap<Integer, String> getConcurrentMap() {
			return this.concurrentMap;
		}

		QueryMap<Integer, String> getQueryMap() {
			return this.queryMap;
		}

		CacheMap<Integer, String> getCacheMap() {
			return this.cacheMap;
		}
	}

	static class WithTransformersBean {
		@Resource(name = COHERENCE_CACHE_BEAN_NAME)
		@Name("people")
		private NamedCache<String, Person> namedCache;

		@Inject
		@Name("people")
		@View(cacheValues = false)
		private ContinuousQueryCache<String, Person, String> keysOnly;

		@Inject
		@Name("people")
		@View
		@PropertyExtractor("firstName")
		private ContinuousQueryCache<String, Person, String> names;

		@Inject
		@Name("people")
		@View
		@ChainedExtractor({"phoneNumber", "number"})
		@WhereFilter("lastName = 'foo'")
		private ContinuousQueryCache<String, Person, String> filteredNames;

		NamedCache<String, Person> getNamedCache() {
			return this.namedCache;
		}

		ContinuousQueryCache<String, Person, String> getNames() {
			return this.names;
		}

		ContinuousQueryCache<String, Person, String> getFilteredNames() {
			return this.filteredNames;
		}

		ContinuousQueryCache<String, Person, String> getKeysOnly() {
			return this.keysOnly;
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
		WithTransformersBean withTransformersBean() {
			return new WithTransformersBean();
		}

		@Bean
		SuperTypesBean superTypesBean() {
			return new SuperTypesBean();
		}

		@Bean
		CtorBean ctorBean(
				@CoherenceCache NamedCache<Integer, String> view,
				@Name("numbers") ContinuousQueryCache<Integer, String, String> numbers) {
			return new CtorBean(view, numbers);
		}

		@Bean
		DifferentSessionsBean differentSessionsBean() {
			return new DifferentSessionsBean();
		}

		@Bean
		ContinuousQueryCacheWithFiltersBean continuousQueryCacheWithFiltersBean() {
			return new ContinuousQueryCacheWithFiltersBean();
		}

		@Bean
		ContinuousQueryCacheFieldsBean continuousQueryCacheFieldsBean() {
			return new ContinuousQueryCacheFieldsBean();
		}

	}
}
