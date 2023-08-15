/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
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
import com.oracle.coherence.spring.test.utils.NetworkUtils;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * Tests for {@link CoherenceIndexedSessionRepository} using gRPC and POF serialization.
 *
 * @author Gunnar Hillert
 */
@DirtiesContext
@SpringJUnitWebConfig
class PofCoherenceIndexedSessionRepositoryTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	static CoherenceClusterMember server;

	PofCoherenceIndexedSessionRepositoryTests() {
		super.sessionName = "grpcSession";
	}

	@BeforeAll
	static void setup() {
		final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
				CacheConfig.of("server-coherence-cache-config.xml"),
				LocalHost.only(),
				IPv4Preferred.yes(),
				SystemProperty.of("coherence.cluster", "PofCoherenceIndexedSessionRepositoryTestsCluster"),
				SystemProperty.of("coherence.grpc.enabled", true),
				SystemProperty.of("coherence.grpc.server.port", "1408"),
				SystemProperty.of("coherence.wka", "127.0.0.1"),
				DisplayName.of("server"),
				SystemProperty.of("tangosol.pof.enabled", "true"));

		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> NetworkUtils.isGrpcPortInUse());
	}

	@AfterAll
	static void cleanup() throws InterruptedException {
		GrpcServerController.INSTANCE.stop();

		if (server != null) {
			server.close();
		}
		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> !NetworkUtils.isGrpcPortInUse());
	}

	@Configuration
	@EnableCoherenceHttpSession(session = "grpcSession")
	@EnableCoherence
	static class CoherenceConfig {
		@Bean
		@DependsOn("grpcChannel")
		GrpcSessionConfigurationBean sessionConfigurationBean(ConfigurableApplicationContext context) {
			GrpcSessionConfigurationBean sessionConfigurationBean = new GrpcSessionConfigurationBean("grpcSession", context);
			sessionConfigurationBean.setChannelName("grpcChannel");
			sessionConfigurationBean.setName("grpcSession");
			return sessionConfigurationBean;
		}

		@Bean
		ManagedChannel grpcChannel() {
			final String host = "localhost";
			final int port = 1408;

			final ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port);
			channelBuilder.usePlaintext();
			return channelBuilder.build();
		}
	}

}
