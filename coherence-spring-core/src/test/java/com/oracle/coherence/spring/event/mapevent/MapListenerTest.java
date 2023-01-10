/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import java.time.LocalDate;
import java.util.List;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.oracle.coherence.spring.event.EventsHelper;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.util.MapEvent;
import data.Person;
import data.PhoneNumber;
import jakarta.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThanOrEqualTo;

/**
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(MapListenerTest.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MapListenerTest {

	private static final Log logger = LogFactory.getLog(MapListenerTest.class);

	@Inject
	@Name("test")
	Session session;

	@Inject
	TestListener listener;

	@Inject
	ConfigurableApplicationContext context;

	@Test
	void testMapEvents() {
		NamedCache<String, Person> people = this.session.getCache("people");

		// Wait for the listener registration as it is async
		Eventually.assertDeferred(() -> EventsHelper.getListenerCount(people), is(greaterThanOrEqualTo(4)));

		people.put("homer", new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("marge", new Person("Marge", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("bart", new Person("Bart", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("lisa", new Person("Lisa", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("maggie", new Person("Maggie", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));

		people.invoke("homer", new UppercaseEntryProcessor());
		people.invoke("bart", new UppercaseEntryProcessor());

		people.remove("bart");
		people.remove("marge");
		people.remove("lisa");
		people.remove("maggie");

		Eventually.assertDeferred(() -> this.listener.getEvents(MapEvent.ENTRY_INSERTED), is(5));
		Eventually.assertDeferred(() -> this.listener.getEvents(MapEvent.ENTRY_UPDATED), is(2));
		Eventually.assertDeferred(() -> this.listener.getEvents(MapEvent.ENTRY_DELETED), is(4));

		// There should be an insert and an update for Bart.
		// The delete for Bart does not match the filter because the lastName
		// had been changed to uppercase.
		List<MapEvent<String, Person>> filteredEvents = this.listener.getFilteredEvents();
		Eventually.assertDeferred(filteredEvents::size, is(2));
		MapEvent<String, Person> eventOne = filteredEvents.get(0);
		MapEvent<String, Person> eventTwo = filteredEvents.get(1);
		assertThat(eventOne.getId(), is(MapEvent.ENTRY_INSERTED));
		assertThat(eventOne.getKey(), is("bart"));
		assertThat(eventTwo.getId(), is(MapEvent.ENTRY_UPDATED));
		assertThat(eventTwo.getKey(), is("bart"));
		assertThat(eventTwo.getNewValue().getLastName(), is("SIMPSON"));

		// Transformed events should just be inserts with the person's firstName as the new value
		List<MapEvent<String, String>> transformedWithExtractorEvents = this.listener.getTransformedWithExtractorEvents();
		Eventually.assertDeferred(transformedWithExtractorEvents::size, is(5));
		assertThat(transformedWithExtractorEvents.get(0).getNewValue(), is("Homer"));
		assertThat(transformedWithExtractorEvents.get(1).getNewValue(), is("Marge"));
		assertThat(transformedWithExtractorEvents.get(2).getNewValue(), is("Bart"));
		assertThat(transformedWithExtractorEvents.get(3).getNewValue(), is("Lisa"));
		assertThat(transformedWithExtractorEvents.get(4).getNewValue(), is("Maggie"));

		// Transformed events should just be inserts with the person's firstName in uppercase as the new value
		List<MapEvent<String, String>> transformedEvents = this.listener.getTransformedEvents();
		Eventually.assertDeferred(transformedEvents::size, is(5));
		assertThat(transformedEvents.get(0).getNewValue(), is("HOMER"));
		assertThat(transformedEvents.get(1).getNewValue(), is("MARGE"));
		assertThat(transformedEvents.get(2).getNewValue(), is("BART"));
		assertThat(transformedEvents.get(3).getNewValue(), is("LISA"));
		assertThat(transformedEvents.get(4).getNewValue(), is("MAGGIE"));
	}

	@Configuration
	@EnableCoherence
	static class Config {

		@Bean
		UppercaseTransformerFactory uppercaseTransformerFactory() {
			return new UppercaseTransformerFactory();
		}

		@Bean
		TestListener testListener() {
			return new TestListener();
		}

		@Bean
		SessionConfigurationBean sessionConfigurationBeanDefault() {
			final SessionConfigurationBean sessionConfigurationBean =
					new SessionConfigurationBean();
			sessionConfigurationBean.setType(SessionType.SERVER);
			sessionConfigurationBean.setConfig("coherence-cache-config.xml");
			sessionConfigurationBean.setName("default");
			return sessionConfigurationBean;
		}

		@Bean
		SessionConfigurationBean sessionConfigurationBeanTest() {
			final SessionConfigurationBean sessionConfigurationBean =
					new SessionConfigurationBean();
			sessionConfigurationBean.setType(SessionType.SERVER);
			sessionConfigurationBean.setConfig("test-coherence-config.xml");
			sessionConfigurationBean.setScopeName("Test");
			sessionConfigurationBean.setName("test");
			return sessionConfigurationBean;
		}
	}
}
