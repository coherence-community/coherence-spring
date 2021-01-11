/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(CoherenceNamedCacheConfigurationTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class CoherenceNamedCacheConfigurationTests {

//	@Autowired
//	@Name("fooCache")
//	private NamedCache<String, String> fooCache;
//
//	@Test
//	@Order(1)
//	public void getDefaultSession() throws Exception {
//		assertThat(this.fooCache).hasSize(0);
//	}
//
	@Configuration
	@EnableCoherence
	static class Config {
	}
}
