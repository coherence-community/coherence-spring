/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import java.io.DataInput;
import java.io.DataOutput;

import com.oracle.coherence.inject.Injectable;
import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.Serializer;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link CoherenceInjector}.
 *
 * @author rl
 * @since 3.0
 */
@SpringJUnitConfig(CoherenceInjectorTest.Config.class)
@DirtiesContext
class CoherenceInjectorTest {

	@Test
	void shouldInjectAfterDeserialization() {
		BeanOne beanOne = new BeanOne();
		assertThat(beanOne.getBeanTwo()).isNull();
		Serializer serializer = new DefaultSerializer();
		Binary binary = ExternalizableHelper.toBinary(beanOne, serializer);
		BeanOne result = ExternalizableHelper.fromBinary(binary, serializer);
		assertThat(result.getBeanTwo()).isNotNull();
	}

	@Configuration
	static class Config {

		@Bean
		CoherenceContext coherenceContext(ApplicationContext applicationContext) {
			return new CoherenceContext(applicationContext);
		}

		@Bean("BeanOne")
		@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		BeanOne getStoreBeanOne() {
			return new BeanOne();
		}

		@Bean("BeanTwo")
		BeanTwo getStoreBeanTwo() {
			return new BeanTwo();
		}
	}

	public static class BeanOne implements ExternalizableLite, Injectable {

		@Autowired
		BeanTwo beanTwo;

		public BeanTwo getBeanTwo() {
			return this.beanTwo;
		}

		@Override
		public void readExternal(DataInput in) {
		}

		@Override
		public void writeExternal(DataOutput out) {
		}
	}

	public static class BeanTwo {
	}
}
