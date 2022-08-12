/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import com.oracle.coherence.spring.annotation.ChainedExtractor;
import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.ExtractorFactory;
import com.oracle.coherence.spring.annotation.PofExtractor;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.io.Serializer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.net.BackingMapContext;
import com.tangosol.net.cache.BackingMapBinaryEntry;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import com.tangosol.util.InvocableMapHelper;
import com.tangosol.util.MapIndex;
import com.tangosol.util.ValueExtractor;
import data.Person;
import data.PhoneNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for Extractor and related annotation annotations.
 *
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(ExtractorConfigurationTests.Config.class)
@DirtiesContext
class ExtractorConfigurationTests {

	private ConfigurablePofContext pofContext = new ConfigurablePofContext("pof-config.xml");

	private Person person;

	private PhoneNumber phoneNumber;

	private Binary binaryKey;

	private Binary binaryPerson;

	private Map.Entry<String, Person> entry;

	@BeforeEach
	void setup() {
		this.phoneNumber = new PhoneNumber(44, "04242424242");
		this.person = new Person("Arthur", "Dent",
				LocalDate.of(1978, 3, 8),
				this.phoneNumber);

		this.binaryKey = ExternalizableHelper.toBinary("AD", this.pofContext);
		this.binaryPerson = ExternalizableHelper.toBinary(this.person, this.pofContext);

		BackingMapContext ctx = mock(BackingMapContext.class);
		Map<ValueExtractor, MapIndex> index = new HashMap<>();

		when(ctx.getIndexMap()).thenReturn(index);

		this.entry = new BackingMapBinaryEntry(this.binaryKey, this.binaryPerson, this.binaryPerson, null) {
			@Override
			public Object getKey() {
				return "AD";
			}

			@Override
			public Object getValue() {
				return ExtractorConfigurationTests.this.person;
			}

			@Override
			public BackingMapContext getBackingMapContext() {
				return ctx;
			}

			@Override
			public Serializer getSerializer() {
				return ExtractorConfigurationTests.this.pofContext;
			}
		};
	}

	@Inject
	ApplicationContext context; //refers to the current application context within the scope of the test

	@Test
	void shouldInjectPropertyExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getFirstNameExtractor(), is(notNullValue()));

		String value = InvocableMapHelper.extractFromEntry(bean.getFirstNameExtractor(), this.entry);
		assertThat(value, is(this.person.getFirstName()));
	}

	@Test
	void shouldInjectMultiPropertyExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getMultiPropertyExtractor(), is(notNullValue()));

		List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiPropertyExtractor(), this.entry);
		assertThat(value, contains(this.person.getFirstName(), this.person.getLastName()));
	}

	@Test
	void shouldInjectChainedExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getChainedExtractor(), is(notNullValue()));

		String value = InvocableMapHelper.extractFromEntry(bean.getChainedExtractor(), this.entry);
		assertThat(value, is(this.person.getPhoneNumber().getNumber()));
	}

	@Test
	void shouldInjectMultiChainedExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getMultiChainedExtractor(), is(notNullValue()));

		List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiChainedExtractor(), this.entry);
		assertThat(value, contains(this.phoneNumber.getCountryCode(), this.phoneNumber.getNumber()));
	}

	@Test
	void shouldInjectCustomExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getCustomExtractor(), is(notNullValue()));

		String value = InvocableMapHelper.extractFromEntry(bean.getCustomExtractor(), this.entry);
		assertThat(value, is(this.person.getLastName()));
	}

	@Test
	void shouldInjectPofExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getPofExtractor(), is(notNullValue()));

		Integer value = InvocableMapHelper.extractFromEntry(bean.getPofExtractor(), this.entry);
		assertThat(value, is(this.person.getPhoneNumber().getCountryCode()));
	}

	@Test
	void shouldInjectMultiPofExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getMultiPofExtractor(), is(notNullValue()));

		List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiPofExtractor(), this.entry);
		assertThat(value, contains(this.phoneNumber.getCountryCode(), this.phoneNumber.getNumber()));
	}

	@Test
	void shouldInjectMultiExtractor() {
		ExtractorBean bean = this.context.getBean(ExtractorBean.class);
		assertThat(bean, is(notNullValue()));
		assertThat(bean.getMultiExtractor(), is(notNullValue()));

		List<?> value = InvocableMapHelper.extractFromEntry(bean.getMultiExtractor(), this.entry);
		assertThat(value, contains(this.person.getLastName(),
				this.person.getFirstName(),
				this.person.getPhoneNumber().getNumber()));
	}

	// ----- helper classes -------------------------------------------------

	@Inherited
	@ExtractorBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface TestExtractor {
	}

	public static class TestExtractorFactory
			implements ExtractorFactory<TestExtractor, Person, String> {
		@Override
		public ValueExtractor<Person, String> create(TestExtractor annotation) {
			return Person::getLastName;
		}
	}

	private static class ExtractorBean {

		@Inject
		@PropertyExtractor("firstName")
		private ValueExtractor<Person, String> firstNameExtractor;

		@Inject
		@ChainedExtractor({"phoneNumber", "number"})
		private ValueExtractor<Person, String> chainedExtractor;

		@Inject
		@TestExtractor
		private ValueExtractor<Person, String> customExtractor;

		@Inject
		@PofExtractor(index = {3, 0})
		private ValueExtractor<Person, Integer> pofExtractor;

		@Inject
		@TestExtractor
		@PropertyExtractor("firstName")
		@ChainedExtractor({"phoneNumber", "number"})
		private ValueExtractor<Person, List<?>> multiExtractor;

		@Inject
		@PropertyExtractor("firstName")
		@PropertyExtractor("lastName")
		private ValueExtractor<Person, List<?>> multiPropertyExtractor;

		@Inject
		@ChainedExtractor({"phoneNumber", "countryCode"})
		@ChainedExtractor({"phoneNumber", "number"})
		private ValueExtractor<Person, List<?>> multiChainedExtractor;

		@Inject
		@PofExtractor(index = {3, 0})
		@PofExtractor(index = {3, 1})
		private ValueExtractor<Person, List<?>> multiPofExtractor;

		ValueExtractor<Person, String> getFirstNameExtractor() {
			return this.firstNameExtractor;
		}

		ValueExtractor<Person, String> getChainedExtractor() {
			return this.chainedExtractor;
		}

		ValueExtractor<Person, String> getCustomExtractor() {
			return this.customExtractor;
		}

		ValueExtractor<Person, Integer> getPofExtractor() {
			return this.pofExtractor;
		}

		ValueExtractor<Person, List<?>> getMultiExtractor() {
			return this.multiExtractor;
		}

		ValueExtractor<Person, List<?>> getMultiPropertyExtractor() {
			return this.multiPropertyExtractor;
		}

		ValueExtractor<Person, List<?>> getMultiChainedExtractor() {
			return this.multiChainedExtractor;
		}

		ValueExtractor<Person, List<?>> getMultiPofExtractor() {
			return this.multiPofExtractor;
		}
	}

	@Configuration
	@EnableCoherence
	static class Config {
		@Bean
		ExtractorBean extractorBean() {
			return new ExtractorBean();
		}

		@Bean
		@TestExtractor
		TestExtractorFactory testExtractorFactory() {
			return new TestExtractorFactory();
		}
	}
}
