/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A {@link CoherenceGrpcClient} that works with Coherence as a config source.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceGrpcClient implements AutoCloseable {

	protected static final Log logger = LogFactory.getLog(CoherenceGrpcClient.class);

	private final Session coherenceSession;
	private Coherence coherence;

	public CoherenceGrpcClient(CoherenceConfigClientProperties coherenceConfigClientProperties) {
		this.coherenceSession = buildSession(coherenceConfigClientProperties);
	}

	public Session getCoherenceSession() {
		return this.coherenceSession;
	}

	/**
	 * Builds Coherence session.
	 * @param coherenceConfigClientProperties must not be null
	 * @return a Coherence Session
	 */
	protected Session buildSession(CoherenceConfigClientProperties coherenceConfigClientProperties) {
		Assert.notNull(coherenceConfigClientProperties, "coherenceConfigClientProperties must not be null");

		final SessionConfiguration.Builder builder = SessionConfiguration.builder();

		if (StringUtils.hasText(coherenceConfigClientProperties.getCacheConfig())) {
			builder.withConfigUri(coherenceConfigClientProperties.getCacheConfig());
		}

		if (StringUtils.hasText(coherenceConfigClientProperties.getSessionName())) {
			builder.named(coherenceConfigClientProperties.getSessionName());
		}

		if (StringUtils.hasText(coherenceConfigClientProperties.getScopeName())) {
			builder.withScopeName(coherenceConfigClientProperties.getScopeName());
		}

		final SessionConfiguration grpcSessionConfiguration = builder.build();

		final CoherenceConfiguration cfg = CoherenceConfiguration.builder()
				.withSession(grpcSessionConfiguration)
				.build();

		this.coherence = Coherence.client(cfg);
		this.coherence.start().join();

		if (StringUtils.hasText(coherenceConfigClientProperties.getSessionName())) {
			return this.coherence.getSession(coherenceConfigClientProperties.getSessionName());
		}
		else {
			return this.coherence.getSession();
		}

	}

	@Override
	public void close() {
		if (this.coherence != null) {
			this.coherence.close();
		}
	}
}
