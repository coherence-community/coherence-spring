/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import java.util.concurrent.TimeUnit;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.coherence.spring.test.utils.NetworkUtils;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceGrpcClientTests {

	static CoherenceClusterMember server;

	@BeforeAll
	static void setup() throws Exception {
		final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
				LocalHost.only(),
				IPv4Preferred.yes(),
				SystemProperty.of("coherence.cluster", "CoherenceGrpcClientTestsCluster"),
				SystemProperty.of("coherence.grpc.enabled", true),
				SystemProperty.of("coherence.grpc.server.port", "1418"),
				SystemProperty.of("coherence.wka", "127.0.0.1"),
				DisplayName.of("server"));

		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> NetworkUtils.isPortInUse(1418));
	}

	@AfterAll
	static void cleanup() {

		if (server != null) {
			server.close();
		}
	}

	@Test
	public void testCoherenceGrpcClient() {
		final CoherenceConfigClientProperties coherenceConfigClientProperties = new CoherenceConfigClientProperties();
		coherenceConfigClientProperties.setCacheConfig("grpc-test-coherence-cache-config2.xml");

		final CoherenceGrpcClient coherenceGrpcClient = new CoherenceGrpcClient(coherenceConfigClientProperties);
		final Session grpcCoherenceSession = coherenceGrpcClient.getCoherenceSession();
		final NamedCache cache = grpcCoherenceSession.getCache("test");
		cache.put("foo2", "bar2");

		coherenceGrpcClient.close();

		final Session coherenceSession = server.getSession();
		assertThat(coherenceSession.getCache("test")).hasSize(1);
		assertThat(coherenceSession.getCache("test").get("foo2")).isEqualTo("bar2");
	}
}
