/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.GrpcSessionConfigurationBean;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
import com.oracle.coherence.spring.test.junit.CoherenceServerJunitExtension;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

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
@ExtendWith(CoherenceServerJunitExtension.class)
@DirtiesContext
@SpringJUnitWebConfig
class PofCoherenceIndexedSessionRepositoryTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	PofCoherenceIndexedSessionRepositoryTests() {
		super.sessionName = "grpcSession";
	}

	@BeforeAll
	static void setup() {
		System.setProperty("tangosol.pof.enabled", "true");
	}

	@AfterAll
	static void teardown() {
		System.clearProperty("tangosol.pof.enabled");
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
