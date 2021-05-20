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
import org.junit.jupiter.api.extension.ExtendWith;

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
@ExtendWith(CoherenceServerJunitExtension.class)
@DirtiesContext
@SpringJUnitWebConfig
public class GrpcSessionCoherenceIndexedSessionRepositoryTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	@Autowired
	private ConfigurableApplicationContext ctx;

	public GrpcSessionCoherenceIndexedSessionRepositoryTests() {
		super.sessionName = "grpcSession";
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
