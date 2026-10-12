/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.boot.mockcache.tests;

import java.lang.reflect.Method;

import com.oracle.coherence.spring.configuration.NamedCacheConfiguration;
import com.oracle.coherence.spring.doc.boot.mockcache.OrderService;
import com.tangosol.net.NamedCache;
import org.junit.jupiter.api.Test;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockingDetails;

class MockCacheLifecycleTests {

	@Test
	void closesAndRecreatesDocumentedContextInSameJvm() throws Exception {
		// An explicit sequence in one test is independent of Surefire's reuseForks=false.
		long processId = ProcessHandle.current().pid();
		TestContextManager manager = new TestContextManager(IsolatedMockCacheTests.class);
		Method method = IsolatedMockCacheTests.class.getDeclaredMethod("readsOrderStatusFromCache");
		ConfigurableApplicationContext previous = null;
		NamedCache<Long, String> previousMock = null;
		OrderService previousService = null;
		int firstIdentity = 0;
		int secondIdentity = 0;
		MockCacheAssertions.assertNoCoherenceInstances();
		manager.beforeTestClass();
		try {
			for (int i = 0; i < 2; i++) {
				MockCacheAssertions.assertNoCoherenceInstances();
				IsolatedMockCacheTests fixture = new IsolatedMockCacheTests();
				try {
					// Bean override and dependency injection listeners run exactly as in SpringExtension.
					manager.prepareTestInstance(fixture);
					ConfigurableApplicationContext context =
							(ConfigurableApplicationContext) manager.getTestContext().getApplicationContext();
					MockCacheAssertions.assertIsolated(context);
					assertThat(context.getBeanNamesForType(NamedCache.class)).hasSize(1);
					assertThat(context.getBean(NamedCacheConfiguration.COHERENCE_CACHE_BEAN_NAME))
							.isSameAs(fixture.orders);
					assertThat(ReflectionTestUtils.getField(fixture.orderService, "orders")).isSameAs(fixture.orders);
					assertThat(mockingDetails(fixture.orders).isMock()).isTrue();
					assertThat(mockingDetails(fixture.orders).getStubbings()).isEmpty();
					assertThat(ProcessHandle.current().pid()).isEqualTo(processId);
					if (previous != null) {
						assertThat(previous.isActive()).as("First context closed before recreation").isFalse();
						assertThat(context).isNotSameAs(previous);
						assertThat(fixture.orders).isNotSameAs(previousMock);
						assertThat(fixture.orderService).isNotSameAs(previousService);
						secondIdentity = System.identityHashCode(context);
					}
					else {
						firstIdentity = System.identityHashCode(context);
					}
					manager.beforeTestMethod(fixture, method);
					manager.beforeTestExecution(fixture, method);
					try {
						fixture.readsOrderStatusFromCache();
						MockCacheAssertions.assertIsolated(context);
					}
					finally {
						manager.afterTestExecution(fixture, method, null);
						manager.afterTestMethod(fixture, method, null);
					}
					previous = context;
					previousMock = fixture.orders;
					previousService = fixture.orderService;
				}
				finally {
					// Evict and close only this test's context; no global Coherence cleanup.
					manager.getTestContext().markApplicationContextDirty(HierarchyMode.CURRENT_LEVEL);
				}
				assertThat(previous.isActive()).isFalse();
				MockCacheAssertions.assertNoCoherenceInstances();
			}
			System.out.printf("Mock cache sequence: pid=%d, contexts=%x,%x, distinct mocks, both closed%n",
					processId, firstIdentity, secondIdentity);
		}
		finally {
			manager.afterTestClass();
			MockCacheAssertions.assertNoCoherenceInstances();
		}
	}

}
