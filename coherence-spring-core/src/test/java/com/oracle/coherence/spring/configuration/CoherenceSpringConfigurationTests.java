/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;

@SpringJUnitConfig(CoherenceSpringConfigurationTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class CoherenceSpringConfigurationTests {

	@Configuration
	@EnableCoherence
	static class Config {

	}

	@Autowired
	private Session defaultCoherenceSession;

	@Autowired
	@Name(Coherence.DEFAULT_NAME)
	private Session defaultCoherenceSessionWithName;

	@Test
	@Order(1)
	public void getDefaultSession() throws Exception {
		Assertions.assertEquals(Coherence.DEFAULT_NAME, this.defaultCoherenceSession.getName());
	}

	@Test
	@Order(2)
	public void getDefaultSessionByName() throws Exception {
		Assertions.assertEquals(Coherence.DEFAULT_NAME, this.defaultCoherenceSessionWithName.getName());
	}

}
