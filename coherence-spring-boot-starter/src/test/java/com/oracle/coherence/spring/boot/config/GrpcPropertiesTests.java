/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceProperties;
import com.oracle.coherence.spring.configuration.session.AbstractSessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.ClientSessionConfigurationBean;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = { GrpcPropertiesTests.class, CoherenceAutoConfiguration.class })
@EnableConfigurationProperties(CoherenceProperties.class)
@ActiveProfiles({"grpcSessionTest"})
@DirtiesContext
public class GrpcPropertiesTests {

	@Autowired
	private CoherenceProperties coherenceProperties;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Test
	void testCoherencePropertiesWithSessions() {
		assertThat(this.coherenceProperties.getSessions().getClient()).isNotNull();
		assertThat(this.coherenceProperties.getSessions().getClient()).hasSize(1);
		assertThat(this.coherenceProperties.getSessions().getClient().get(0).getName()).isEqualTo("grpc-session");
	}

	@Test
	void testThatApplicationContextContainsGrpcSessionConfigurationBean() {
		assertThat(this.applicationContext.getBeansOfType(AbstractSessionConfigurationBean.class)).hasSize(1);
		final ClientSessionConfigurationBean grpcSessionConfigurationBean = this.applicationContext.getBean(ClientSessionConfigurationBean.class);
		assertThat(grpcSessionConfigurationBean.getName()).isEqualTo("grpc-session");
	}
}
