/*
 * Copyright (c) 2020, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.scan.StandardJarScanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Gunnar Hillert
 */
public class Server {

	private static final int DEFAULT_PORT = 8080;
	private final Tomcat tomcat;

	public static void main(String[] args) throws Exception {
		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		new Server().run();
	}

	public Server() throws Exception {
		this(getPort());
	}

	/**
	 * Creates a Tomcat instance with the specified port
	 * @param port the port of the default connector
	 * @throws IOException when underlying temp directories cannot be created
	 */
	public Server(int port) throws IOException {
		tomcat = new Tomcat();
		tomcat.setPort(port);
		tomcat.getConnector(); //Needed for Tomcat 9

		@SuppressWarnings("java:S5443") // Sonar
		final File tempDirectory = Files.createTempDirectory("coherence-spring-demo").toFile();
		tomcat.setBaseDir(tempDirectory.getAbsolutePath());
		final Context tomcatContext = tomcat.addWebapp("", tempDirectory.getAbsolutePath());
		((StandardJarScanner) tomcatContext.getJarScanner()).setScanManifest(false);
	}

	/**
	 * Start the server.
	 * @throws LifecycleException indicate Tomcat lifecycle related problems
	 */
	public void run() throws LifecycleException {
		tomcat.start();
		tomcat.getServer().await();
	}

	/**
	 * Start server and return when server has started.
	 * @throws LifecycleException indicate Tomcat lifecycle related problems
	 */
	public void start() throws LifecycleException {
		tomcat.start();
	}

	public void stop() throws Exception {
		try {
			tomcat.stop();
		} finally {
			tomcat.destroy();
		}
	}

	private static int getPort() throws IOException {
		return System.getenv("PORT") == null ? DEFAULT_PORT : Integer.parseInt(System.getenv("PORT"));
	}
}
