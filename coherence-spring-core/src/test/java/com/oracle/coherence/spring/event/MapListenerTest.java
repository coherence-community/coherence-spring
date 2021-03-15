/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.common.collections.ConcurrentHashMap;
import com.oracle.coherence.spring.annotation.MapEventTransformerBinding;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.annotation.event.CacheName;
import com.oracle.coherence.spring.annotation.event.Deleted;
import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.ScopeName;
import com.oracle.coherence.spring.annotation.event.ServiceName;
import com.oracle.coherence.spring.annotation.event.Synchronous;
import com.oracle.coherence.spring.annotation.event.Updated;
import com.oracle.coherence.spring.configuration.MapEventTransformerFactory;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import data.Person;
import data.PhoneNumber;
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

@SpringJUnitConfig(MapListenerTest.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MapListenerTest {
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

		people.invoke("homer", new Uppercase());
		people.invoke("bart", new Uppercase());

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

	// ---- helper classes --------------------------------------------------

	public static class Uppercase
			implements InvocableMap.EntryProcessor<String, Person, Object> {
		@Override
		public Object process(InvocableMap.Entry<String, Person> entry) {
			Person p = entry.getValue();
			p.setLastName(p.getLastName().toUpperCase());
			entry.setValue(p);
			return null;
		}
	}

	public static class TestListener {
		private final Map<Integer, Integer> events = new ConcurrentHashMap<>();

		private final List<MapEvent<String, Person>> filteredEvents = Collections.synchronizedList(new ArrayList<>());

		private final List<MapEvent<String, String>> transformedEvents = Collections.synchronizedList(new ArrayList<>());

		private final List<MapEvent<String, String>> transformedWithExtractorEvents = Collections.synchronizedList(new ArrayList<>());

		Integer getEvents(int id) {
			return this.events.get(id);
		}

		public List<MapEvent<String, Person>> getFilteredEvents() {
			return this.filteredEvents;
		}

		public List<MapEvent<String, String>> getTransformedEvents() {
			return this.transformedEvents;
		}

		public List<MapEvent<String, String>> getTransformedWithExtractorEvents() {
			return this.transformedWithExtractorEvents;
		}

		@Synchronous
		@WhereFilter("firstName = 'Bart' and lastName = 'Simpson'")
		@CoherenceEventListener
		void onHomer(@CacheName("people") MapEvent<String, Person> event) {
			this.filteredEvents.add(event);
		}

		@Synchronous
		@CoherenceEventListener
		void onPersonDeleted(@Deleted @CacheName("people") MapEvent<String, Person> event) {
			record(event);
		}

		@Synchronous
		@CoherenceEventListener
		void onPersonInserted(@Inserted @ScopeName("Test") @MapName("people") MapEvent<String, Person> event) {
			record(event);
			assertThat(event.getNewValue().getLastName(), is("Simpson"));
		}

		@Synchronous
		@PropertyExtractor("firstName")
		@CoherenceEventListener
		void onPersonInsertedTransformedWithExtractor(@Inserted @MapName("people") MapEvent<String, String> event) {
            this.transformedWithExtractorEvents.add(event);
		}

		@Synchronous
		@UppercaseName
		@CoherenceEventListener
		void onPersonInsertedTransformed(@Inserted @MapName("people") MapEvent<String, String> event) {
            this.transformedEvents.add(event);
		}

		@Synchronous
		@CoherenceEventListener
		void onPersonUpdated(@Updated @ServiceName("StorageService") @MapName("people") MapEvent<String, Person> event) {
			record(event);
			assertThat(event.getOldValue().getLastName(), is("Simpson"));
			assertThat(event.getNewValue().getLastName(), is("SIMPSON"));
		}

		private void record(MapEvent<String, Person> event) {
			this.events.compute(event.getId(), (k, v) -> (v != null) ? v + 1 : 1);
		}
	}

	@Inherited
	@MapEventTransformerBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	public @interface UppercaseName {
	}


	@UppercaseName
	public static class UppercaseTransformerFactory implements MapEventTransformerFactory<UppercaseName, String, Person, String> {
		@Override
		public MapEventTransformer<String, Person, String> create(UppercaseName annotation) {
			return new UppercaseNameTransformer();
		}
	}

	/**
	 * A custom implementation of a {@link MapEventTransformer}.
	 */
	static class UppercaseNameTransformer implements MapEventTransformer<String, Person, String> {

		@Override
		@SuppressWarnings("unchecked")
		public MapEvent<String, String> transform(MapEvent<String, Person> event) {
			String sOldName = transform(event.getOldValue());
			String sNewName = transform(event.getNewValue());
			return new MapEvent<String, String>(event.getMap(), event.getId(), event.getKey(), sOldName, sNewName);
		}

		String transform(Person person) {
			if (person == null) {
				return null;
			}
			String name = person.getFirstName();
			return (name != null) ? name.toUpperCase() : null;
		}
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
