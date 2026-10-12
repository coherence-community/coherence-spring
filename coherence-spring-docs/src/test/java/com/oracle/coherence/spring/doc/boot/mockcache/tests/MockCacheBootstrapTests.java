/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.boot.mockcache.tests;

import com.oracle.coherence.spring.doc.boot.mockcache.ProductionApplication;
import com.tangosol.net.NamedCache;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.BootstrapUtils;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class MockCacheBootstrapTests {

	private static final String EXCLUSIONS = "spring.autoconfigure.exclude="
			+ "com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration,"
			+ "com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration,"
			+ "com.oracle.coherence.spring.boot.autoconfigure.data.CoherenceRepositoriesAutoConfiguration";

	@Test
	void typeOnlyMockCannotResolveCoherenceMap() throws Exception {
		assertMissingCacheProxy(new TypeOnlyMock());
	}

	@Test
	void logicalCacheNameCannotResolveCoherenceMap() throws Exception {
		assertMissingCacheProxy(new LogicalNameMock());
	}

	@Test
	void testConfigurationStillDiscoversProductionApplication() {
		MockCacheAssertions.assertNoCoherenceInstances();
		// Inspect Boot's actual selection without creating the production context.
		assertThat(BootstrapUtils.resolveTestContextBootstrapper(DiscoveredApplication.class)
				.buildMergedContextConfiguration().getClasses()).contains(ProductionApplication.class);
		assertThat(BootstrapUtils.resolveTestContextBootstrapper(SupplementalConfiguration.class)
				.buildMergedContextConfiguration().getClasses()).contains(ProductionApplication.class);
		assertThat(BootstrapUtils.resolveTestContextBootstrapper(IsolatedMockCacheTests.class)
				.buildMergedContextConfiguration().getClasses())
				.containsExactly(IsolatedMockCacheTests.TestApplication.class);
		MockCacheAssertions.assertNoCoherenceInstances();
	}

	private void assertMissingCacheProxy(Object fixture) throws Exception {
		MockCacheAssertions.assertNoCoherenceInstances();
		TestContextManager manager = new TestContextManager(fixture.getClass());
		manager.beforeTestClass();
		try {
			// Use the real listeners so @MockitoBean overrides run before dependency injection.
			Throwable failure = catchThrowable(() -> manager.prepareTestInstance(fixture));
			assertThat(failure).hasRootCauseInstanceOf(SpelEvaluationException.class)
					.hasStackTraceContaining("getCacheProxy");
		}
		finally {
			// Also closes a context if a regression unexpectedly allows preparation to succeed.
			manager.getTestContext().markApplicationContextDirty(HierarchyMode.CURRENT_LEVEL);
			manager.afterTestClass();
			MockCacheAssertions.assertNoCoherenceInstances();
		}
	}

	@SpringBootTest(classes = IsolatedMockCacheTests.TestApplication.class)
	@TestPropertySource(properties = EXCLUSIONS)
	static class TypeOnlyMock {

		@MockitoBean
		NamedCache<Long, String> orders;

	}

	@SpringBootTest(classes = IsolatedMockCacheTests.TestApplication.class)
	@TestPropertySource(properties = EXCLUSIONS)
	static class LogicalNameMock {

		@MockitoBean(name = "orders")
		NamedCache<Long, String> orders;

	}

	@SpringBootTest
	static class DiscoveredApplication {
	}

	@SpringBootTest(classes = SupplementalConfiguration.ExtraConfiguration.class)
	static class SupplementalConfiguration {

		@TestConfiguration(proxyBeanMethods = false)
		static class ExtraConfiguration {
		}

	}

}
