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
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.ClientSessionConfigurationBean;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import com.oracle.coherence.spring.test.utils.IsGrpcProxyRunning;
import com.oracle.coherence.spring.test.utils.NetworkUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;

/**
 * Tests for {@link CoherenceIndexedSessionRepository} using gRPC and POF serialization.
 *
 * @author Gunnar Hillert
 */
@DirtiesContext
@SpringJUnitWebConfig
@TestPropertySource(properties = {
		"coherence.tcmp.enabled = 'false'",
		"coherence-spring.test-cluster-name = " + PofCoherenceIndexedSessionRepositoryTests.CLUSTER_NAME
})
class PofCoherenceIndexedSessionRepositoryTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	static final String CLUSTER_NAME = "PofCoherenceIndexedSessionRepositoryTestsCluster";

	static CoherenceClusterMember server;

	PofCoherenceIndexedSessionRepositoryTests() {
		super.sessionName = "grpcSession";
	}

	protected String getLocalClusterName() {
		return CLUSTER_NAME;
	}

@BeforeAll
	static void setup() {
		final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
				CacheConfig.of("server-coherence-cache-config.xml"),
				LocalHost.only(),
				IPv4Preferred.yes(),
				SystemProperty.of("coherence.cluster", CLUSTER_NAME),
				SystemProperty.of("coherence.grpc.enabled", true),
				SystemProperty.of("coherence.wka", "127.0.0.1"),
				DisplayName.of("server"),
				SystemProperty.of("tangosol.pof.enabled", "true"));
		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> server.invoke(IsGrpcProxyRunning.INSTANCE));
	}

	@AfterAll
	static void cleanup() {
		if (server != null) {
			server.close();
		}
		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> !NetworkUtils.isGrpcPortInUse());
	}

	@Configuration
	@EnableCoherenceHttpSession(session = "grpcSession")
	@EnableCoherence
	@Import(AbstractCoherenceIndexedSessionRepositoryTests.CommonConfig.class)
	static class CoherenceConfig {
		@Bean
		ClientSessionConfigurationBean sessionConfigurationBean() {
			ClientSessionConfigurationBean sessionConfigurationBean = new ClientSessionConfigurationBean();
			sessionConfigurationBean.setName("grpcSession");
			sessionConfigurationBean.setConfig("grpc-test-coherence-cache-config.xml");
			return sessionConfigurationBean;
		}
	}
}
