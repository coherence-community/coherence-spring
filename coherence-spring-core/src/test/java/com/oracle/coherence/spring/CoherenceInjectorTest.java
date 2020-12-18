package com.oracle.coherence.spring;

import com.oracle.coherence.inject.Injectable;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.ExternalizableLite;
import com.tangosol.io.Serializer;
import com.tangosol.util.Binary;
import com.tangosol.util.ExternalizableHelper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.DataInput;
import java.io.DataOutput;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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
		assertThat(beanOne.getBeanTwo(), is(nullValue()));
		Serializer serializer = new DefaultSerializer();
		Binary binary = ExternalizableHelper.toBinary(beanOne, serializer);
		BeanOne result = ExternalizableHelper.fromBinary(binary, serializer);
		assertThat(result.getBeanTwo(), is(notNullValue()));
	}

	@Configuration
	@ComponentScan
	static class Config {
		@Bean(name="BeanOne")
		@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
		public BeanOne getStoreBeanOne() {
			return new BeanOne();
		}

		@Bean(name="BeanTwo")
		public BeanTwo getStoreBeanTwo() {
			return new BeanTwo();
		}
	}

	public static class BeanOne implements ExternalizableLite, Injectable {

		@Autowired
		BeanTwo beanTwo;

		public BeanTwo getBeanTwo() {
			return beanTwo;
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
