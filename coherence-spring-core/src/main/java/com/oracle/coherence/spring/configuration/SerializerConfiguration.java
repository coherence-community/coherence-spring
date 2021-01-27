/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import com.oracle.coherence.common.base.Classes;
import com.tangosol.io.Serializer;
import com.tangosol.net.Cluster;
import com.tangosol.net.OperationalContext;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures support for Coherence {@link com.tangosol.io.Serializer} beans.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
public class SerializerConfiguration {

	/**
	 * The Coherence {@link OperationalContext}.
	 */
	private final OperationalContext operationalContext;

	SerializerConfiguration(Cluster cluster) {
		this.operationalContext = (OperationalContext) cluster;
	}

	/**
	 * A factory method to produce the default Java {@link Serializer}.
	 * @return the default Java {@link Serializer}
	 */
	@Qualifier("java")
	@Bean
	public Serializer defaultSerializer() {
		return this.operationalContext.getSerializerMap().get("java")
				.createSerializer(Classes.getContextClassLoader());
	}

	/**
	 * A factory method to produce the default
	 * Java {@link Serializer}.
	 * @return the default Java {@link Serializer}
	 */
	@Qualifier("pof")
	@Bean
	public Serializer pofSerializer() {
		return this.operationalContext.getSerializerMap().get("pof")
				.createSerializer(Classes.getContextClassLoader());
	}
}
