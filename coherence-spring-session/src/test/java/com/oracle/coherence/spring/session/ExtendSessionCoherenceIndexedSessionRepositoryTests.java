/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.callables.IsServiceRunning;
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.ClientSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

import static org.hamcrest.CoreMatchers.is;

/**
 * Tests for {@link CoherenceIndexedSessionRepository} using Coherence*Extend.
 *
 * @author Gunnar Hillert
 */
@DirtiesContext
@SpringJUnitWebConfig
public class ExtendSessionCoherenceIndexedSessionRepositoryTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	public ExtendSessionCoherenceIndexedSessionRepositoryTests() {
		super.sessionName = "coherence_extend";
	}

	static CoherenceClusterMember server;

	@BeforeAll
	static void setup() {

		final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
			CacheConfig.of("server-coherence-cache-config.xml"),
			LocalHost.only(),
			IPv4Preferred.yes(),
			SystemProperty.of("coherence.cluster", "ExtendSessionTestsCluster"),
			SystemProperty.of("coherence.wka", "127.0.0.1"),
			DisplayName.of("server"));

		// Wait for Coherence to start
		Eventually.assertDeferred(() -> server.invoke(new IsServiceRunning("ExtendTcpCacheService")), is(true));

	}

	@AfterAll
	static void cleanup() {
		if (server != null) {
			server.close();
		}
	}

	@Configuration
	@EnableCoherenceHttpSession(session = "coherence_extend") //TODO Derive from single session
	@EnableCoherence
	static class CoherenceConfig {
		@Bean
		SessionConfigurationBean sessionConfigurationBean() {
			SessionConfigurationBean sb = new ClientSessionConfigurationBean();
			sb.setConfig("client-coherence-cache-config.xml");
			sb.setName("coherence_extend");
			return sb;
		}
	}
}
