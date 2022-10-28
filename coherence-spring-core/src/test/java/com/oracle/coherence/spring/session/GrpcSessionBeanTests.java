/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.spring.session;

import java.util.Map;

import com.oracle.coherence.client.GrpcSessionConfiguration;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.GrpcSessionConfigurationBean;
import com.oracle.coherence.spring.test.junit.CoherenceServerJunitExtension;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(GrpcSessionBeanTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class GrpcSessionBeanTests {

	@RegisterExtension
	static CoherenceServerJunitExtension coherenceServerJunitExtension =
			new CoherenceServerJunitExtension(true);

	final Coherence coherence;

	public GrpcSessionBeanTests(Coherence coherence) {
		this.coherence = coherence;
	}

	@Test
	@Order(1)
	public void testBasicGrpcClient() throws Exception {
		final GrpcSessionConfiguration.Builder builder = GrpcSessionConfiguration.builder();
		final Session session = Session.create(builder.build()).get();
		final Map<String, String> fooMap = session.getMap("fooMap");
		fooMap.put("foo", "bar");

		final Map<String, String> mapFromCoherence = this.coherence.getSession().getMap("fooMap");
		assertEquals("bar", mapFromCoherence.get("foo"));
	}

	@Configuration
	@EnableCaching
	@EnableCoherence
	static class Config {
		@Bean
		GrpcSessionConfigurationBean grpcSessionConfigurationBean() {
			final GrpcSessionConfigurationBean sessionConfigurationBean = new GrpcSessionConfigurationBean();
			sessionConfigurationBean.setName(GrpcSessionConfigurationBean.DEFAULT_SESSION_NAME);
			return sessionConfigurationBean;
		}
	}
}
