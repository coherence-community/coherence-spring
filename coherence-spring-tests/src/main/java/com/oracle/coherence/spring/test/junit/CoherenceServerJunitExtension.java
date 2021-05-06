/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.coherence.spring.test.junit;

import java.net.URL;
import java.net.URLClassLoader;

import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.SessionConfiguration;
import io.github.classgraph.ClassGraph;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Provides an embedded Coherence Cluster as a Junit5 extension.
 * @author Gunnar Hillert
 */
public class CoherenceServerJunitExtension implements ParameterResolver,
		BeforeAllCallback, AfterAllCallback {

	protected static final Log logger = LogFactory.getLog(CoherenceServerJunitExtension.class);

	private Coherence coherence;

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
		System.setProperty("coherence.log", "slf4j");
		if (logger.isInfoEnabled()) {
			logger.info("JunitExtension - Starting up Coherence...");
		}
		final SessionConfiguration.Builder sessionConfigurationBuilder = (SessionConfiguration.Builder) getFromTestClassloader(SessionConfiguration.class)
				.getMethod("builder")
				.invoke(null);
		final SessionConfiguration sessionConfiguration = sessionConfigurationBuilder
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
