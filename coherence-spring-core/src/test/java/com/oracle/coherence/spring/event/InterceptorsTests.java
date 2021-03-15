/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
import com.oracle.coherence.spring.annotation.event.Updated;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.Coherence;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig(InterceptorsTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InterceptorsTests {

	@Inject
	ConfigurableApplicationContext context;

	@Inject
	@Name("test")
	private Session session;

	@Inject
	private TestObservers observers;

	@Inject
	private Coherence coherence;

	@Test
	void testEventInterceptorMethods() {

		CompletableFuture<Void> closeFuture = this.coherence.whenClosed();

		// Ensure that Coherence has started before stating the test
		this.coherence.whenStarted().join();

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

		this.coherence.getCluster().shutdown();
		this.coherence.close();
		//context.close();

		// ensure that Coherence is closed so that we should have the Stopped event
		closeFuture.join();

		this.observers.getEvents().forEach(System.err::println);

		assertThat(this.observers.getEvents(), hasItems(
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
		private final Map<Enum<?>, Boolean> events = new ConcurrentHashMap<>();

		List<Enum<?>> getEvents() {
			return this.events.keySet().stream().sorted(Comparator.comparing(Enum::name)).collect(Collectors.toList());
		}

		// cache lifecycle events
		@CoherenceEventListener
		void onCacheLifecycleEvent(@ServiceName("StorageService") CacheLifecycleEvent event) {
			record(event);
		}

		// Coherence lifecycle events
		@CoherenceEventListener
		void onCoherenceLifecycleEvent(CoherenceLifecycleEvent event) {
			record(event);
		}

		// Session lifecycle events
		@CoherenceEventListener
		void onSessionLifecycleEvent(SessionLifecycleEvent event) {
			record(event);
		}

		@CoherenceEventListener
		void onCreatedPeople(@Created @MapName("people") CacheLifecycleEvent event) {
			record(event);
			assertThat(event.getCacheName(), is("people"));
		}

		@CoherenceEventListener
		void onDestroyedPeople(@Destroyed @CacheName("people") CacheLifecycleEvent event) {
			record(event);
			assertThat(event.getCacheName(), is("people"));
		}

		// entry events
		@CoherenceEventListener
		void onEntryEvent(@MapName("people") EntryEvent<String, Person> event) {
			record(event);
		}

		@CoherenceEventListener
		void onExecuted(@Executed @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
			record(event);
			assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
			assertThat(event.getEntrySet().size(), is(0));
		}

		@CoherenceEventListener
		void onExecuting(@Executing @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
			record(event);
			assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
			//assertThat(event.getEntrySet().size(), is(5));
		}

		// lifecycle events
		@CoherenceEventListener
		void onLifecycleEvent(LifecycleEvent event) {
			record(event);
		}

		@CoherenceEventListener
		void onPersonInserted(@Inserted @CacheName("people") EntryEvent<String, Person> event) {
			record(event);
			assertThat(event.getValue().getLastName(), is("Simpson"));
		}

		@CoherenceEventListener
		void onPersonRemoved(@Removed @CacheName("people") EntryEvent<String, Person> event) {
			record(event);
			assertThat(event.getOriginalValue().getLastName(), is("SIMPSON"));
		}

		@CoherenceEventListener
		void onPersonUpdated(@Updated @CacheName("people") EntryEvent<String, Person> event) {
			record(event);
			assertThat(event.getValue().getLastName(), is("SIMPSON"));
		}

		// transaction events
		@CoherenceEventListener
		void onTransactionEvent(TransactionEvent event) {
			record(event);
		}

		// transfer events
		@CoherenceEventListener
		void onTransferEvent(@ScopeName("Test") @ServiceName("StorageService") TransferEvent event) {
			record(event);
		}

		void record(Event<?> event) {
			this.events.put(event.getType(), true);
		}
	}

	@Configuration
	@EnableCoherence
	static class Config {

		@Bean
		//@Profile("InterceptorsTest")
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
