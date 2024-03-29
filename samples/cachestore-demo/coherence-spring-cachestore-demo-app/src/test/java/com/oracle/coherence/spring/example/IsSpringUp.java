/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example;

import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.tangosol.net.management.MBeanHelper;

public class IsSpringUp implements RemoteCallable<Boolean> {

    public static final IsSpringUp INSTANCE = new IsSpringUp();

	@Override
	@SuppressWarnings("rawtypes")
	public Boolean call() throws Exception {
		try {
			MBeanServer server = MBeanHelper.findMBeanServer();
			ObjectName name = new ObjectName("org.springframework.boot:type=Endpoint,name=Health");
			Map map = (Map) server.invoke(name, "health", new Object[0], new String[0]);
			String status = String.valueOf(map.get("status"));
			return "UP".equalsIgnoreCase(status);
		} catch (Throwable ex) {
			return false;
		}
	}

}
