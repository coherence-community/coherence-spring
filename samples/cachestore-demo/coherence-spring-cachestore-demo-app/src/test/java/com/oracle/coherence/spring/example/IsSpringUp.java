/*
 * Copyright (c) 2021, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example;

import java.lang.management.ManagementFactory;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;

public class IsSpringUp implements RemoteCallable<Boolean> {

	public static final IsSpringUp INSTANCE = new IsSpringUp();

	static final String OBJECT_NAME = "org.springframework.boot:type=Admin,name=SpringApplication";

	@Override
	public Boolean call() throws Exception {
		return isReady(ManagementFactory.getPlatformMBeanServer());
	}

	static boolean isReady(MBeanServer server) throws JMException {
		try {
			return (Boolean) server.getAttribute(new ObjectName(OBJECT_NAME), "Ready");
		}
		catch (InstanceNotFoundException ex) {
			// Boot has not registered the application-admin MBean yet.
			return false;
		}
	}

}
