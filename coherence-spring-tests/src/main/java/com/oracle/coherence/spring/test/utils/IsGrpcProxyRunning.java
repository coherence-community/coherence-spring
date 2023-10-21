/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.test.utils;

import com.oracle.bedrock.runtime.coherence.callables.IsServiceRunning;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.tangosol.net.grpc.GrpcDependencies;

/**
 * A Bedrock {@link RemoteCallable} to check whether the system gRPC proxy is running.
 *
 * @author Gunnar Hillert
 */
public class IsGrpcProxyRunning extends IsServiceRunning {

	/**
	 * The singleton instance of {@link IsGrpcProxyRunning}.
	 */
	public static final IsGrpcProxyRunning INSTANCE = new IsGrpcProxyRunning();

	/**
	 * Constructor that will initialize the super constructor of {@link IsServiceRunning}
	 * with {@link GrpcDependencies#SCOPED_PROXY_SERVICE_NAME}.
	 */
	public IsGrpcProxyRunning() {
		super(GrpcDependencies.SCOPED_PROXY_SERVICE_NAME);
	}

	/**
	 * Convenience method to call {@link super#call()}.
	 * @return true if the gRPC proxy is running
	 */
	public static boolean locally() {
		return INSTANCE.call();
	}
}
