/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.Serializer;
import com.tangosol.io.pof.ConfigurablePofContext;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(SerializerConfigurationTests.Config.class)
@DirtiesContext
class SerializerConfigurationTests {

	@Autowired
	ConfigurableApplicationContext context;

	@Test
	void shouldGetJavaSerializer() {
		final Serializer serializer = BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.context.getBeanFactory(), Serializer.class, "java");
		assertThat(serializer).isNotNull();
		assertThat(serializer).isInstanceOf(DefaultSerializer.class);
	}

	@Test
	void shouldGetPofSerializer() {
		final Serializer serializer = BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.context.getBeanFactory(), Serializer.class, "pof");
		assertThat(serializer).isNotNull();
		assertThat(serializer).isInstanceOf(ConfigurablePofContext.class);
	}

	@Test
	void shouldNotGetJsonSerializer() {
		try {
			BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.context.getBeanFactory(), Serializer.class, "json");
		}
		catch (NoSuchBeanDefinitionException ex) {
			assertThat(ex.getMessage()).isEqualTo("No bean named 'json' available: No matching Serializer bean found for" +
					" qualifier 'json' - neither qualifier match nor bean name match!");
			return;
		}
		fail("Expected a ");
	}

	@Configuration
	@EnableCoherence
	static class Config {
	}
}
