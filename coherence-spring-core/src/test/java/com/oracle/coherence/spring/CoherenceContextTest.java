/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for {@link CoherenceContext}.
 */
@SpringJUnitConfig(CoherenceContextTest.Config.class)
@DirtiesContext
public class CoherenceContextTest {


	@Autowired
	ApplicationContext applicationContext;

	@Test
	void shouldSupplyContext() {
		assertThat(CoherenceContext.getApplicationContext(), is(applicationContext));
	}

	@Configuration
	@ComponentScan
	static class Config {
	}
}
