/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import com.oracle.coherence.spring.test.junit.CoherenceServerJunitExtension;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceGrpcClientTests {

	@RegisterExtension
	static CoherenceServerJunitExtension coherenceServerJunitExtension =
			new CoherenceServerJunitExtension(true);

	final Coherence coherence;

	public CoherenceGrpcClientTests(Coherence coherence) {
		this.coherence = coherence;
	}

	@Test
	public void testDefaultCacheManagerExists() throws Exception {
		final CoherenceConfigClientProperties coherenceConfigClientProperties = new CoherenceConfigClientProperties();
		coherenceConfigClientProperties.getClient().setHost("localhost");
		coherenceConfigClientProperties.getClient().setPort(1408);
		coherenceConfigClientProperties.getClient().setEnableTls(false);

		final CoherenceGrpcClient coherenceGrpcClient = new CoherenceGrpcClient(coherenceConfigClientProperties);
		final Session grpcCoherenceSession = coherenceGrpcClient.getCoherenceSession();
		final NamedCache cache = grpcCoherenceSession.getCache("test");
		cache.put("foo2", "bar2");

		coherenceGrpcClient.close();

		final Session coherenceSession = this.coherence.getSession();
		assertThat(coherenceSession.getCache("test")).hasSize(1);
		assertThat(coherenceSession.getCache("test").get("foo2")).isEqualTo("bar2");
	}
}
