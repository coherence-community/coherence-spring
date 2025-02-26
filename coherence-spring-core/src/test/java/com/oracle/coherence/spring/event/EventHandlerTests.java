/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.Synchronous;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.util.InvocableMap;
import data.Person;
import data.PhoneNumber;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringJUnitConfig(EventHandlerTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class EventHandlerTests {

	@Inject
	ConfigurableApplicationContext context;

	@Inject
	@Name("test")
	private Session session;

	@Inject
	private TestObservers observers;

	@Inject
	private CoherenceServer coherenceServer;


	@Test
	@DirtiesContext
	void testEventInterceptorMethods() {
		this.observers.asyncMethodEvents.clear();
		this.observers.syncMethodEvents.clear();
		NamedCache<String, Person> people = this.session.getCache("people");
		people.put("homer", new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		// inserting
		// inserted
		people.invokeAll(new Uppercase());
		// updating
		// updated
		people.clear();
		// removing
		// removed

		people.truncate();
		people.destroy();

		List<EventInfo> expectedSyncMethod = List.of(
				new EventInfo(EntryEvent.Type.INSERTING, true),
				new EventInfo(EntryEvent.Type.INSERTED, true),
				new EventInfo(EntryEvent.Type.UPDATING, true),
				new EventInfo(EntryEvent.Type.UPDATED, true),
				new EventInfo(EntryEvent.Type.REMOVING, true),
				new EventInfo(EntryEvent.Type.REMOVED, true));

		List<EventInfo> expectedAsyncMethod = List.of(
				new EventInfo(EntryEvent.Type.INSERTING, true),
				new EventInfo(EntryEvent.Type.INSERTED, false),
				new EventInfo(EntryEvent.Type.UPDATING, true),
				new EventInfo(EntryEvent.Type.UPDATED, false),
				new EventInfo(EntryEvent.Type.REMOVING, true),
				new EventInfo(EntryEvent.Type.REMOVED, false));

		assertIterableEquals(expectedAsyncMethod, this.observers.asyncMethodEvents);
		assertIterableEquals(expectedSyncMethod, this.observers.syncMethodEvents);
		this.coherenceServer.stop();
	}

	/**
	 * A simple entry processor to convert a {@link Person} last name to upper case.
	 */
	public static class Uppercase implements InvocableMap.EntryProcessor<String, Person, Object> {
		@Override
		public Object process(InvocableMap.Entry<String, Person> entry) {
			Person p = entry.getValue();
			p.setLastName(p.getLastName().toUpperCase());
			entry.setValue(p);
			return null;
		}
	}

	public record EventInfo(EntryEvent.Type type, boolean sync) {
	}

	public static class TestObservers {
		Queue<EventInfo> syncMethodEvents = new LinkedList<>();
		Queue<EventInfo> asyncMethodEvents = new LinkedList<>();

		@Synchronous
		@CoherenceEventListener
		void onEntryEventSync(@MapName("people") EntryEvent<String, Person> event) {
			this.syncMethodEvents.add(new EventInfo(event.getType(), isSync()));
		}

		@CoherenceEventListener
		void onEntryEventAsync(@MapName("people") EntryEvent<String, Person> event) {
			this.asyncMethodEvents.add(new EventInfo(event.getType(), isSync()));
		}

		private boolean isSync() {
			return !Thread.currentThread().getName().contains("ForkJoinPool");
		}
	}

	static class DummyService {

	}

	@Configuration
	@EnableCoherence
	static class Config {

		@Bean
		TestObservers testObservers() {
			return new TestObservers();
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
