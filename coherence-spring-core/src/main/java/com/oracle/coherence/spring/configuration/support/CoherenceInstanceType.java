/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

/**
 * An enum representing the type of the {@link com.tangosol.net.Coherence} instance.
 *
 * @author Gunnar Hillert
 * @since 3.0
 * @see com.tangosol.net.Coherence
 */
public enum CoherenceInstanceType {

	/**
	 * Represents a Coherence client instance e.g. used when configuring Coherence*Extend or gRPC clients.
	 */
	CLIENT,

	/**
	 * The instance is a cluster member.
	 */
	CLUSTER,

}
