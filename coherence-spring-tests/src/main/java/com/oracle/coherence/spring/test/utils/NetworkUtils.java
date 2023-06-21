/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.test.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class NetworkUtils {

	/**
	 * Helper method to determine if the default gRPC port is available or not.
	 * @return true if the gRPC port is bound and thus unavailable
	 */
	public static boolean isGrpcPortInUse() {

		boolean result = false;

		try {
			(new Socket(InetAddress.getLoopbackAddress(), 1408)).close();
			result = true;
		}
		catch (IOException ex) {
		}
		return result;
	}
}
