/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.coherence.spring.boot.config;

import java.util.Optional;

import com.oracle.coherence.client.GrpcSessionConfiguration;
import com.tangosol.net.Session;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
	private final ManagedChannel grpcChannel;

	public CoherenceGrpcClient(CoherenceConfigClientProperties coherenceConfigClientProperties) {
		this.grpcChannel = this.buildChannel(coherenceConfigClientProperties);
		this.coherenceSession = buildSession(this.grpcChannel, coherenceConfigClientProperties);
	}

	public Session getCoherenceSession() {
		return this.coherenceSession;
	}

	/**
	 * Builds Coherence session.
	 * @param grpcChannel mMust not be null
	 * @param coherenceConfigClientProperties must not be null
	 * @return a Coherence Session
	 */
	protected Session buildSession(Channel grpcChannel, CoherenceConfigClientProperties coherenceConfigClientProperties) {
		Assert.notNull(grpcChannel, "grpcChannel must not be null");
		Assert.notNull(coherenceConfigClientProperties, "coherenceConfigClientProperties must not be null");

		final GrpcSessionConfiguration.Builder builder = GrpcSessionConfiguration.builder(grpcChannel);

		if (StringUtils.hasText(coherenceConfigClientProperties.getSessionName())) {
			builder.named(coherenceConfigClientProperties.getSessionName());
		}

		if (StringUtils.hasText(coherenceConfigClientProperties.getScopeName())) {
			builder.withScopeName(coherenceConfigClientProperties.getScopeName());
		}

		final GrpcSessionConfiguration grpcSessionConfiguration = builder.build();

		final Optional<Session> optional = Session.create(grpcSessionConfiguration);
		return optional.orElseGet(() -> {
			throw new IllegalStateException("Unable to create session.");
		});
	}

	/**
	 * Builds gRPC channel.
	 * @param coherenceConfigClientProperties must not be null
	 * @return gRPC channel
	 */
	protected ManagedChannel buildChannel(CoherenceConfigClientProperties coherenceConfigClientProperties) {
		Assert.notNull(coherenceConfigClientProperties, "coherenceConfigClientProperties must not be null");

		final String host = coherenceConfigClientProperties.getClient().getHost();
		final int port = coherenceConfigClientProperties.getClient().getPort();

		ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder.forAddress(host, port);
		if (!coherenceConfigClientProperties.getClient().isEnableTls()) {
			channelBuilder.usePlaintext();
		}
		return channelBuilder.build();
	}

	@Override
	public void close() {
		this.grpcChannel.shutdownNow();
	}
}
