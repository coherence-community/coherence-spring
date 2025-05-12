/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.Synchronous;
import com.oracle.coherence.spring.configuration.annotation.CoherenceCache;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.MapEvent;
import data.Person;
import data.PhoneNumber;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@SpringJUnitConfig(MapEventHandlerTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MapEventHandlerTests {

	@Inject
	ConfigurableApplicationContext context;

	@Inject
	@Name("test")
	private Session session;

	@Inject
	private TestObservers observers;

	@Inject
	private CoherenceServer coherenceServer;

	@CoherenceCache
	@SessionName("test")
	private NamedCache<String, Person> people;

	@Test
	@DirtiesContext
	void testEventInterceptorMethods() {
		this.observers.asyncMethodEvents.clear();
		this.observers.syncMethodEvents.clear();
		// saturate ForkJoinPool to make listener registration slow
		for (int i = 0; i < 8; i++) {
			ForkJoinPool.commonPool().execute(() -> {
				try {
					Thread.sleep(300);
				}
				catch (InterruptedException ex) {
				}
			});
		}

		this.people.put("homer", new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		// inserting
		// inserted
		this.people.invokeAll(new Uppercase());
		// updating
		// updated
		this.people.clear();
		// removing
		// removed

		List<EventInfo> expectedSyncMethod = List.of(
				new EventInfo(MapEvent.ENTRY_INSERTED, true),
				new EventInfo(MapEvent.ENTRY_UPDATED, true),
				new EventInfo(MapEvent.ENTRY_DELETED, true));

		List<EventInfo> expectedAsyncMethod = List.of(
				new EventInfo(MapEvent.ENTRY_INSERTED, false),
				new EventInfo(MapEvent.ENTRY_UPDATED, false),
				new EventInfo(MapEvent.ENTRY_DELETED, false));

		Eventually.assertDeferred(() -> this.observers.syncMethodEvents, is(equalTo(expectedSyncMethod)));
		Eventually.assertDeferred(() -> new HashSet<>(this.observers.asyncMethodEvents), is(equalTo(new HashSet<>(expectedAsyncMethod))));

		this.people.truncate();
		this.people.destroy();
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

	public record EventInfo(int type, boolean sync) {
	}

	public static class TestObservers {
		final Queue<EventInfo> syncMethodEvents = new LinkedList<>();
		final Queue<EventInfo> asyncMethodEvents = new ConcurrentLinkedQueue<>();

		@Synchronous
		@CoherenceEventListener
		void onMapEventSync(@MapName("people") MapEvent<String, Person> event) {
			this.syncMethodEvents.add(new EventInfo(event.getId(), isSync()));
		}

		@CoherenceEventListener
		void onMapEventAsync(@MapName("people") MapEvent<String, Person> event) {
			this.asyncMethodEvents.add(new EventInfo(event.getId(), isSync()));
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
