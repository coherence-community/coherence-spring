/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.event;

import java.util.concurrent.TimeUnit;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.event.EventsHelper;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

/**
*
* @author Gunnar Hillert
*
*/
@WebAppConfiguration
@SpringBootTest(classes = {
		ServletWebServerFactoryAutoConfiguration.class,
		CoherenceEventListenerTests.Config.class
})
public class CoherenceEventListenerTests {

	@Autowired
	private Coherence coherence;

	@Autowired
	private TestService testService;

	@Test
	@DirtiesContext
	public void testCoherenceEventListener() {
		final NamedMap<String, String> namedMap = this.coherence.getSession().getMap("tasks");

		// Wait for the listener registration as it is async
		Eventually.assertDeferred(() -> EventsHelper.getListenerCount(namedMap), is(greaterThanOrEqualTo(1)));

		for (int i = 1; i <= 100; i++) {
			namedMap.put("foo_" + i, "bar" + i);
		}

		namedMap.remove("foo_1");

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() ->
			assertThat(this.testService.getEventNames()).hasSize(101));
		assertThat(this.testService.getEventNames()).contains("insert", "insert", "delete");

	}

	@Configuration
	@EnableCoherence
	@EnableAutoConfiguration
	@ComponentScan({ "com.oracle.coherence.spring.boot.tests.event" })
	static class Config {
	}

}
