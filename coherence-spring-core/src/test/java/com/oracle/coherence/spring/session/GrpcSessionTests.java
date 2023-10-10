/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

package com.oracle.coherence.spring.session;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.coherence.grpc.proxy.GrpcServerController;
import com.oracle.coherence.spring.configuration.annotation.CoherenceCache;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.ClientSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.test.utils.NetworkUtils;
import com.tangosol.net.NamedCache;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(GrpcSessionTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
		"coherence.tcmp.enabled = 'false'"
})
@DirtiesContext
public class GrpcSessionTests {

	static CoherenceClusterMember server;

	@CoherenceCache
	private NamedCache<String, String> fooMap;

	@BeforeAll
	static void setup() throws Exception {
		final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
				LocalHost.only(),
				IPv4Preferred.yes(),
				SystemProperty.of("coherence.cluster", "GrpcSessionTestsCluster"),
				SystemProperty.of("coherence.grpc.enabled", true),
				SystemProperty.of("coherence.grpc.server.port", "1408"),
				SystemProperty.of("coherence.wka", "127.0.0.1"),
				DisplayName.of("server"));

		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> NetworkUtils.isGrpcPortInUse());
	}

	@AfterAll
	static void cleanup() {
		GrpcServerController.INSTANCE.stop();
		if (server != null) {
			server.close();
		}
		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> !NetworkUtils.isGrpcPortInUse());
	}

	@Test
	@Order(1)
	public void testBasicGrpcClient() throws Exception {
		this.fooMap.put("foo", "bar");
		final Map<String, String> mapFromCoherence = this.server.getSession().getMap("fooMap");
		assertEquals("bar", mapFromCoherence.get("foo"));
	}

	@Configuration
	@EnableCoherence
	static class Config {
		@Bean
		ClientSessionConfigurationBean grpcSessionConfigurationBean() {
			final ClientSessionConfigurationBean sessionConfigurationBean = new ClientSessionConfigurationBean();
			sessionConfigurationBean.setName(SessionConfigurationBean.DEFAULT_SESSION_NAME);
			sessionConfigurationBean.setConfig("grpc-core-test-coherence-cache-config.xml");
			return sessionConfigurationBean;
		}
	}
}
