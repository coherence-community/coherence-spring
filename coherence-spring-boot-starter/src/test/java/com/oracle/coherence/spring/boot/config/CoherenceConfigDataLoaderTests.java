/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.coherence.spring.test.utils.NetworkUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootTest(
		properties = { "coherence.tcmp.enabled=false" },
		classes = {
			CoherenceConfigDataLoaderTests.DataLoaderConfig.class,
			CoherenceConfigClientProperties.class
		})
@ActiveProfiles("custom")
@DirtiesContext
public class CoherenceConfigDataLoaderTests {

	private static CoherenceClusterMember server;

	@Autowired
	private Environment env;

	@BeforeAll
	static void setup() throws Exception {
		final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
				LocalHost.only(),
				IPv4Preferred.yes(),
				SystemProperty.of("coherence.cluster", "CoherenceConfigDataLoaderTests"),
				SystemProperty.of("coherence.grpc.enabled", true),
				SystemProperty.of("coherence.grpc.server.port", "1408"),
				SystemProperty.of("coherence.wka", "127.0.0.1"),
				DisplayName.of("server"));

		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> NetworkUtils.isGrpcPortInUse());
	}

	@AfterAll
	static void cleanup() {
		if (server != null) {
			server.close();
		}
	}

	public CoherenceConfigDataLoaderTests() {
		final Map<String, Object> properties = server.getSession().getMap("berlin-kona");
		//properties.put("foo", "remote");
		properties.put("test.foo", "bar");
		properties.put("test.numeric", 1234);
	}

	@Test
	public void testDataLoader() {
			assertThat(this.env.getProperty("foo")).isEqualTo("bar");
			assertThat(this.env.getProperty("test.foo")).isEqualTo("bar");
	}

	@Configuration
	static class DataLoaderConfig {
	}

}
