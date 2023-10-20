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
import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.DisplayName;
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
@SpringJUnitConfig(CoherenceExtendTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
		"coherence.tcmp.enabled = 'false'"
})
@DirtiesContext
public class CoherenceExtendTests {

	static CoherenceClusterMember server;

	@CoherenceCache
	private NamedCache<String, String> fooMap;

	private static int extendPort = 0;

	@BeforeAll
	static void setup() throws Exception {
			final LocalPlatform platform = LocalPlatform.get();

		// Start the Coherence server
		server = platform.launch(CoherenceClusterMember.class,
				LocalHost.only(),
				IPv4Preferred.yes(),
				CacheConfig.of("coherence-extend-server-cache-config.xml"),
				SystemProperty.of("coherence.cluster", "CoherenceExtendTestsCluster"),
				SystemProperty.of("coherence.grpc.enabled", false),
				SystemProperty.of("coherence.extend.enabled", true),
				SystemProperty.of("coherence.wka", "127.0.0.1"),
				DisplayName.of("server"));

		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> NetworkUtils.isPortInUse(7574));
		Thread.sleep(2000);
	}

	@AfterAll
	static void cleanup() {
		if (server != null) {
			server.close();
		}
		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(() -> !NetworkUtils.isPortInUse(7574));
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
		ClientSessionConfigurationBean coherenceExtendSessionConfigurationBean() {
			final ClientSessionConfigurationBean sessionConfigurationBean = new ClientSessionConfigurationBean();
			sessionConfigurationBean.setName(SessionConfigurationBean.DEFAULT_SESSION_NAME);
			sessionConfigurationBean.setConfig("extend-core-test-coherence-cache-config-nameservice.xml");
			return sessionConfigurationBean;
		}
	}
}
