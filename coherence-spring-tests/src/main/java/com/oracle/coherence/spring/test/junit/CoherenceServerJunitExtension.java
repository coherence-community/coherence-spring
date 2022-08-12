/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.test.junit;

import java.net.URL;
import java.net.URLClassLoader;

import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.SessionConfiguration;
import io.github.classgraph.ClassGraph;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an embedded Coherence Cluster as a Junit5 extension.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceServerJunitExtension implements ParameterResolver,
		BeforeAllCallback, AfterAllCallback {

	protected static final Logger logger = LoggerFactory.getLogger(CoherenceServerJunitExtension.class);

	private final String configUri;

	private Coherence coherence;

	public CoherenceServerJunitExtension() {
		this.configUri = "coherence-cache-config.xml";
	}

	public CoherenceServerJunitExtension(String configUri) {
		this.configUri = configUri;
	}

	@Override
	public void afterAll(ExtensionContext context) {
		this.coherence.getCluster().shutdown();
		this.coherence.close();
		System.clearProperty("coherence.log");
		if (logger.isInfoEnabled()) {
			logger.info("Shutting down Coherence complete.");
		}
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		System.setProperty("coherence.grpc.server.port", "1408");
		System.setProperty("coherence.log", "slf4j");
		if (logger.isInfoEnabled()) {
			logger.info("JunitExtension - Starting up Coherence...");
		}
		final SessionConfiguration.Builder sessionConfigurationBuilder = (SessionConfiguration.Builder) getFromTestClassloader(SessionConfiguration.class)
				.getMethod("builder")
				.invoke(null);
		final SessionConfiguration sessionConfiguration = sessionConfigurationBuilder
				.withConfigUri(this.configUri)
				.build();

		final CoherenceConfiguration.Builder coherenceBuilder = (CoherenceConfiguration.Builder) getFromTestClassloader(CoherenceConfiguration.class)
				.getMethod("builder")
				.invoke(null);
		final CoherenceConfiguration cfg = coherenceBuilder
				.withSessions(sessionConfiguration)
				.build();

		this.coherence = (Coherence) getFromTestClassloader(Coherence.class)
				.getMethod("clusterMember", CoherenceConfiguration.class)
				.invoke(null, cfg);
		this.coherence.start().join();
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return parameterContext.getParameter()
				.getType()
				.equals(Coherence.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		return this.coherence;
	}

	private Class<?> getFromTestClassloader(Class<?> clazz) throws ClassNotFoundException {
		final ClassLoader testClassLoader = new TestClassLoader();
		return Class.forName(clazz.getName(), true, testClassLoader);
	}

	static class TestClassLoader extends URLClassLoader {
		TestClassLoader() {
			super(new ClassGraph().getClasspathURLs().toArray(new URL[0]));
		}
	}
}
