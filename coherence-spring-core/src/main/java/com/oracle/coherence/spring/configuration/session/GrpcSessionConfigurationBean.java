/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.session;

import java.util.Optional;

import com.oracle.coherence.client.GrpcSessionConfiguration;
import com.tangosol.io.Serializer;
import com.tangosol.net.SessionConfiguration;
import io.grpc.Channel;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A {@link com.oracle.coherence.client.GrpcSessionConfiguration} bean that will be
 * created for each named session in the application configuration properties.
 * <p>
 * This configuration bean specifically produces {@link GrpcSessionConfiguration}
 * beans <i>only</i> if the configuration has a {@code channelName} property. The
 * {@code channelName} refers to the name of a {@link io.grpc.Channel} bean.
 * <p>
 * Sessions are configured with the {@code coherence.session} prefix,
 * for example {@code coherence.session.foo} configures a session named
 * foo.
 * <p>
 * The session name {@code default} is a special case that configures
 * the default session named {@link com.tangosol.net.Coherence#DEFAULT_NAME}.
 * @author Gunnar Hillert
 * @since 3.0
 * @see GrpcSessionConfiguration
 * @see ClientSessionConfigurationBean
 * @deprecated As of 4.0 please configure the Grpc Client using the coherence-cache-config.xml
 */
@Deprecated(
		since = "4.0.0",
		forRemoval = true
)
public class GrpcSessionConfigurationBean extends AbstractSessionConfigurationBean {

	/**
	 * The Spring context.
	 */
	private ConfigurableApplicationContext ctx;

	/**
	 * The name of the gRPC {@link com.tangosol.net.messaging.Channel} bean.
	 */
	private String channelName;

	/**
	 * The name of the {@link Serializer} bean.
	 */
	private String serializer;

	/**
	 * {@code true} to enable distributed tracing for the gRPC methods.
	 */
	private boolean tracingEnabled;

	/**
	 * Create a named {@link GrpcSessionConfigurationBean}.
	 * @param name the name for the session
	 * @param ctx  the Spring application context
	 */
	public GrpcSessionConfigurationBean(String name, ConfigurableApplicationContext ctx) {
		super(name);
		super.setType(SessionType.GRPC);
		this.ctx = ctx;
	}

	public GrpcSessionConfigurationBean() {
		super.setType(SessionType.GRPC);
	}

	@Override
	public Optional<SessionConfiguration> getConfiguration() {
		if (this.getType() != SessionType.GRPC) {
			return Optional.empty();
		}

		GrpcSessionConfiguration.Builder builder;

		if (this.channelName == null || this.channelName.trim().isEmpty()) {
			builder = GrpcSessionConfiguration.builder();
		}
		else {
			try {
				final Channel bean = BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.ctx.getBeanFactory(), Channel.class, this.channelName);
				builder = GrpcSessionConfiguration.builder(bean);
			}
			catch (NoSuchBeanDefinitionException ex) {
				builder = GrpcSessionConfiguration.builder(this.channelName);
			}
		}
		builder = builder.named(getName())
				.withScopeName(getScopeName())
				.withTracing(this.tracingEnabled)
				.withPriority(getPriority());

		if (this.serializer != null && this.serializer.trim().isEmpty()) {
			try {
				final Serializer bean = BeanFactoryAnnotationUtils.qualifiedBeanOfType(this.ctx.getBeanFactory(), Serializer.class, this.serializer);
				builder.withSerializer(bean, this.serializer);
			}
			catch (NoSuchBeanDefinitionException ex) {
				builder.withSerializerFormat(this.serializer);
			}
		}

		return Optional.of(builder.build());
	}

	/**
	 * Set the name of the gRPC {@link io.grpc.Channel} bean.
	 * @param channelName the name of the gRPC {@link io.grpc.Channel} bean
	 */
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	/**
	 * Set the name of the {@link Serializer}.
	 * @param serializer the name of the {@link Serializer}
	 */
	public void setSerializer(String serializer) {
		this.serializer = serializer;
	}

	/**
	 * Set whether distributed tracing should be enabled.
	 * @param enabled {@code true} to enable distributed tracing
	 */
	public void setTracing(boolean enabled) {
		this.tracingEnabled = enabled;
	}
}
