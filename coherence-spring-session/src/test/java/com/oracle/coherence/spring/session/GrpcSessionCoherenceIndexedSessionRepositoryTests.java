/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.util.concurrent.TimeUnit;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.coherence.grpc.proxy.GrpcServerController;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.GrpcSessionConfigurationBean;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * Tests for {@link CoherenceIndexedSessionRepository} using gRPC.
 *
 * @author Gunnar Hillert
 */
@DirtiesContext
@SpringJUnitWebConfig
public class GrpcSessionCoherenceIndexedSessionRepositoryTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	static CoherenceClusterMember server;

	@Autowired
	private ConfigurableApplicationContext ctx;

	public GrpcSessionCoherenceIndexedSessionRepositoryTests() {
		super.sessionName = "grpcSession";
	}

	@BeforeAll
	static void setup() throws Exception {
		final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
				CacheConfig.of("server-coherence-cache-config.xml"),
				LocalHost.only(),
				IPv4Preferred.yes(),
				SystemProperty.of("coherence.grpc.enabled", true),
				SystemProperty.of("coherence.grpc.server.port", "1408"),
				DisplayName.of("server"));

		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> isGrpcPortInUse());
	}

	@AfterAll
	static void cleanup() {
		GrpcServerController.INSTANCE.stop();
		if (server != null) {
			server.close();
		}
		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> !isGrpcPortInUse());
	}

	@Configuration
	@EnableCoherenceHttpSession(session = "grpcSession")
	@EnableCoherence
	static class CoherenceConfig {
		@Bean
		GrpcSessionConfigurationBean sessionConfigurationBean() {
			GrpcSessionConfigurationBean sessionConfigurationBean = new GrpcSessionConfigurationBean();
			sessionConfigurationBean.setName("grpcSession");
			return sessionConfigurationBean;
		}
	}

}
