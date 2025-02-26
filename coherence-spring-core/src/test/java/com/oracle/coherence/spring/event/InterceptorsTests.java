/*
 * Copyright (c) 2013, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.event.CacheName;
import com.oracle.coherence.spring.annotation.event.Created;
import com.oracle.coherence.spring.annotation.event.Destroyed;
import com.oracle.coherence.spring.annotation.event.Executed;
import com.oracle.coherence.spring.annotation.event.Executing;
import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.Processor;
import com.oracle.coherence.spring.annotation.event.Removed;
import com.oracle.coherence.spring.annotation.event.ScopeName;
import com.oracle.coherence.spring.annotation.event.ServiceName;
import com.oracle.coherence.spring.annotation.event.Synchronous;
import com.oracle.coherence.spring.annotation.event.Updated;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.SessionLifecycleEvent;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;
import com.tangosol.util.InvocableMap;
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
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(InterceptorsTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class InterceptorsTests {

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

		NamedCache<String, Person> people = this.session.getCache("people");

		people.put("homer", new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("marge", new Person("Marge", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("bart", new Person("Bart", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("lisa", new Person("Lisa", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
		people.put("maggie", new Person("Maggie", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));

		people.invokeAll(new Uppercase());

		people.clear();
		people.truncate();
		people.destroy();

		this.coherenceServer.stop();

		this.observers.getEvents().entrySet().forEach((entry) ->
			System.out.println(entry.getKey() + " - "
			+ StringUtils.collectionToCommaDelimitedString(entry.getValue())));

		assertThat(this.observers.getUniqueEventsNames().size()).isGreaterThan(24);

		assertThat(this.observers.getUniqueEventsNames(), hasItems(
				LifecycleEvent.Type.ACTIVATED,
				LifecycleEvent.Type.ACTIVATING,
				TransferEvent.Type.ASSIGNED,
				TransactionEvent.Type.COMMITTED,
				TransactionEvent.Type.COMMITTING,
				CacheLifecycleEvent.Type.CREATED,
				CacheLifecycleEvent.Type.DESTROYED,
				LifecycleEvent.Type.DISPOSING,
				EntryProcessorEvent.Type.EXECUTED,
				EntryProcessorEvent.Type.EXECUTING,
				EntryEvent.Type.INSERTED,
				EntryEvent.Type.INSERTING,
				EntryEvent.Type.REMOVED,
				EntryEvent.Type.REMOVING,
				CoherenceLifecycleEvent.Type.STARTED,
				CoherenceLifecycleEvent.Type.STARTING,
				CoherenceLifecycleEvent.Type.STOPPED,
				SessionLifecycleEvent.Type.STOPPING,
				EntryEvent.Type.UPDATED,
				EntryEvent.Type.UPDATING,
				CacheLifecycleEvent.Type.TRUNCATED
		));
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

	public static class TestObservers {
		private final Map<String, Set<Enum<?>>> events = new ConcurrentHashMap<>();

		List<Enum<?>> getUniqueEventsNames() {
			final List<Enum<?>> eventNames = this.events.values().stream()
				.flatMap(Set::stream)
				.filter((e) -> {
					if (e != null) {
						return true;
					}
					return false;
				})
				.distinct()
				.sorted(Comparator.comparing(Enum::name))
				.collect(Collectors.toList());
			return eventNames;
		}

		Map<String, Set<Enum<?>>> getEvents() {
			return this.events;
		}

		// cache lifecycle events
		@Synchronous
		@CoherenceEventListener
		void onCacheLifecycleEvent(@ServiceName("StorageService") CacheLifecycleEvent event) {
			record("onCacheLifecycleEvent", event);
		}

		// Coherence lifecycle events
		@Synchronous
		@CoherenceEventListener
		void onCoherenceLifecycleEvent(CoherenceLifecycleEvent event) {
			record("onCoherenceLifecycleEvent", event);
		}

		// Session lifecycle events
		@Synchronous
		@CoherenceEventListener
		void onSessionLifecycleEvent(SessionLifecycleEvent event) {
			record("onSessionLifecycleEvent", event);
		}

		@Synchronous
		@CoherenceEventListener
		void onCreatedPeople(@Created @MapName("people") CacheLifecycleEvent event) {
			record("onCreatedPeople", event);
			assertThat(event.getCacheName(), is("people"));
		}

		@Synchronous
		@CoherenceEventListener
		void onDestroyedPeople(@Destroyed @CacheName("people") CacheLifecycleEvent event) {
			record("onDestroyedPeople", event);
			assertThat(event.getCacheName(), is("people"));
		}

		// entry events
		@Synchronous
		@CoherenceEventListener
		void onEntryEvent(@MapName("people") EntryEvent<String, Person> event) {
			record("onEntryEvent", event);
		}

		@Synchronous
		@CoherenceEventListener
		void onExecuted(@Executed @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
			record("onExecuted", event);
			assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
			assertThat(event.getEntrySet().size(), is(5));
		}

		@Synchronous
		@CoherenceEventListener
		void onExecuting(@Executing @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
			record("onExecuting", event);
			assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
			assertThat(event.getEntrySet().size(), is(5));
		}

		// lifecycle events
		@Synchronous
		@CoherenceEventListener
		void onLifecycleEvent(LifecycleEvent event) {
			record("onLifecycleEvent", event);
		}

		@Synchronous
		@CoherenceEventListener
		void onPersonInserted(@Inserted @CacheName("people") EntryEvent<String, Person> event) {
			record("onPersonInserted", event);
			assertThat(event.getValue().getLastName(), is("Simpson"));
		}

		@Synchronous
		@CoherenceEventListener
		void onPersonRemoved(@Removed @CacheName("people") EntryEvent<String, Person> event) {
			record("onPersonRemoved", event);
			assertThat(event.getOriginalValue().getLastName(), is("SIMPSON"));
		}

		@Synchronous
		@CoherenceEventListener
		void onPersonUpdated(@Updated @CacheName("people") EntryEvent<String, Person> event) {
			record("onPersonUpdated", event);
			assertThat(event.getValue().getLastName(), is("SIMPSON"));
		}

		// transaction events
		@Synchronous
		@CoherenceEventListener
		void onTransactionEvent(TransactionEvent event) {
			record("onTransactionEvent", event);
		}

		// transfer events
		@Synchronous
		@CoherenceEventListener
		void onTransferEvent(@ScopeName("Test") @ServiceName("StorageService") TransferEvent event) {
			record("onTransferEvent", event);
		}

		void record(String listenerName, Event<?> event) {
			final Set<Enum<?>> entry = this.events.getOrDefault(listenerName, new HashSet<Enum<?>>());
			entry.add(event.getType());
			this.events.put(listenerName, entry);
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
