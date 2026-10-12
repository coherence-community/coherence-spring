/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
// tag::example[]
package com.oracle.coherence.spring.doc.boot.mockcache.tests;

import com.oracle.coherence.spring.configuration.NamedCacheConfiguration;
import com.oracle.coherence.spring.doc.boot.mockcache.OrderService;
import com.tangosol.net.NamedCache;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = IsolatedMockCacheTests.TestApplication.class)
@TestPropertySource(properties = {
	"spring.autoconfigure.exclude="
		+ "com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration,"
		+ "com.oracle.coherence.spring.boot.autoconfigure.session."
		+ "CoherenceSpringSessionAutoConfiguration,"
		+ "com.oracle.coherence.spring.boot.autoconfigure.data."
		+ "CoherenceRepositoriesAutoConfiguration"
})
class IsolatedMockCacheTests {

	@MockitoBean(name = NamedCacheConfiguration.COHERENCE_CACHE_BEAN_NAME)
	NamedCache<Long, String> orders;

	@Autowired
	OrderService orderService;

	@Test
	void readsOrderStatusFromCache() {
		when(this.orders.get(42L)).thenReturn("SHIPPED");

		assertEquals("SHIPPED", this.orderService.getStatus(42L));
		verify(this.orders).get(42L);
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@Import(OrderService.class)
	static class TestApplication {
	}

}
// end::example[]
