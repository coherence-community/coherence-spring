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

/**
 * A collection of network related utilities.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class NetworkUtils {

	/**
	 * Helper method to determine if a port is available or not.
	 * @param port the port to check
	 * @return true if the port is bound and thus unavailable
	 */
	public static boolean isPortInUse(Integer port) {
		boolean result = false;

		try {
			(new Socket(InetAddress.getLoopbackAddress(), port)).close();
			result = true;
		}
		catch (IOException ex) {
		}
		return result;
	}

	/**
	 * Helper method to determine if the default gRPC port 1408 is available or not.
	 * @return true if the gRPC port is bound and thus unavailable
	 */
	public static boolean isGrpcPortInUse() {
		return isPortInUse(1408);
	}
}
