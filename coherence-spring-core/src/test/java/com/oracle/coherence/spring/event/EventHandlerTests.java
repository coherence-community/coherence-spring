/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.annotation.event.Synchronous;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.oracle.coherence.spring.event.liveevent.handler.EventHandler;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.util.InvocableMap;
import data.Person;
import data.PhoneNumber;
import jakarta.inject.Inject;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
	void testEventInterceptorMethods() throws InterruptedException {
		this.observers.asyncMethodEvents.clear();
		this.observers.syncMethodEvents.clear();
		NamedCache<String, Person> people = this.session.getCache("people");

		try {
			people.put("homer", new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
			people.invokeAll(new Uppercase());
			// Only this observer's offloaded UPDATED callback waits. REMOVING remains
			// inline and must be recorded before that callback is released.
			assertTrue(this.observers.updatedStarted.await(30, TimeUnit.SECONDS), "UPDATED callback did not start");
			people.clear();
			this.observers.releaseUpdated.countDown();
			boolean completed = this.observers.asyncCallbacksCompleted.await(30, TimeUnit.SECONDS);
			this.observers.assertNoCallbackFailures();
			assertTrue(completed, "Asynchronous observer callbacks did not complete");

			Eventually.assertDeferred(() -> this.observers.asyncMethodEvents, entryEventContract(false));
			Eventually.assertDeferred(() -> this.observers.syncMethodEvents, entryEventContract(true));
			assertTrue(precedes(List.copyOf(this.observers.asyncMethodEvents),
					EntryEvent.Type.REMOVING, EntryEvent.Type.UPDATED), "REMOVING must be recorded before UPDATED");

			people.truncate();
			people.destroy();
			this.coherenceServer.stop();
		}
		finally {
			this.observers.releaseUpdated.countDown();
			if (this.observers.updatedStarted.getCount() == 0) {
				assertTrue(this.observers.updatedCompleted.await(30, TimeUnit.SECONDS), "UPDATED callback did not finish");
			}
			this.observers.assertNoCallbackFailures();
		}
	}

	private static Matcher<Collection<EventInfo>> entryEventContract(boolean synchronousObserver) {
		return new EntryEventContract(synchronousObserver);
	}

	private static List<EventInfo> expectedEvents(boolean synchronousObserver) {
		return List.of(
				new EventInfo(EntryEvent.Type.INSERTING, true),
				new EventInfo(EntryEvent.Type.INSERTED, synchronousObserver),
				new EventInfo(EntryEvent.Type.UPDATING, true),
				new EventInfo(EntryEvent.Type.UPDATED, synchronousObserver),
				new EventInfo(EntryEvent.Type.REMOVING, true),
				new EventInfo(EntryEvent.Type.REMOVED, synchronousObserver));
	}

	private static String contractMismatch(List<EventInfo> actual, boolean synchronousObserver) {
		List<EventInfo> expected = expectedEvents(synchronousObserver);
		if (actual.size() != expected.size() || !new HashSet<>(actual).equals(new HashSet<>(expected))) {
			return "expected each event once with its delivery mode " + expected + " but was " + actual;
		}
		if (!precedes(actual, EntryEvent.Type.INSERTING, EntryEvent.Type.UPDATING)
				|| !precedes(actual, EntryEvent.Type.UPDATING, EntryEvent.Type.REMOVING)) {
			return "pre-events were not recorded in operation order: " + actual;
		}
		if (!precedes(actual, EntryEvent.Type.INSERTING, EntryEvent.Type.INSERTED)
				|| !precedes(actual, EntryEvent.Type.UPDATING, EntryEvent.Type.UPDATED)
				|| !precedes(actual, EntryEvent.Type.REMOVING, EntryEvent.Type.REMOVED)) {
			return "a post-event was recorded before its pre-event: " + actual;
		}
		if (synchronousObserver && (!precedes(actual, EntryEvent.Type.INSERTED, EntryEvent.Type.UPDATED)
				|| !precedes(actual, EntryEvent.Type.UPDATED, EntryEvent.Type.REMOVED))) {
			return "synchronous post-events were not recorded in commit order: " + actual;
		}
		return null;
	}

	private static boolean precedes(List<EventInfo> events, EntryEvent.Type first, EntryEvent.Type second) {
		int firstIndex = indexOfType(events, first);
		int secondIndex = indexOfType(events, second);
		return firstIndex >= 0 && secondIndex > firstIndex;
	}

	private static int indexOfType(List<EventInfo> events, EntryEvent.Type type) {
		for (int i = 0; i < events.size(); i++) {
			if (events.get(i).type() == type) {
				return i;
			}
		}
		return -1;
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

	/**
	 * Completeness and delivery mode are required for both observers. Operation order is
	 * required for pre-events, and commit order is required for post-events only when the
	 * observer is synchronous. An asynchronous post-event has no happens-before relationship
	 * with a later operation's pre-event.
	 */
	private static final class EntryEventContract extends TypeSafeMatcher<Collection<EventInfo>> {

		private final boolean synchronousObserver;

		private EntryEventContract(boolean synchronousObserver) {
			this.synchronousObserver = synchronousObserver;
		}

		@Override
		protected boolean matchesSafely(Collection<EventInfo> events) {
			return contractMismatch(List.copyOf(events), this.synchronousObserver) == null;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("entry events honoring the ")
				.appendText(this.synchronousObserver ? "synchronous" : "asynchronous")
				.appendText(" observer contract");
		}

		@Override
		protected void describeMismatchSafely(Collection<EventInfo> events, Description description) {
			description.appendText(contractMismatch(List.copyOf(events), this.synchronousObserver));
		}
	}

	public static class TestObservers {
		final Queue<EventInfo> syncMethodEvents = new ConcurrentLinkedQueue<>();
		final Queue<EventInfo> asyncMethodEvents = new ConcurrentLinkedQueue<>();
		final Queue<Throwable> callbackFailures = new ConcurrentLinkedQueue<>();
		final CountDownLatch updatedStarted = new CountDownLatch(1);
		final CountDownLatch releaseUpdated = new CountDownLatch(1);
		final CountDownLatch updatedCompleted = new CountDownLatch(1);
		final CountDownLatch asyncCallbacksCompleted = new CountDownLatch(6);

		@Synchronous
		@CoherenceEventListener
		void onEntryEventSync(@MapName("people") EntryEvent<String, Person> event) {
			this.syncMethodEvents.add(new EventInfo(event.getType(), isInline()));
		}

		@CoherenceEventListener
		void onEntryEventAsync(@MapName("people") EntryEvent<String, Person> event) {
			try {
				boolean inline = isInline();
				if (event.getType() == EntryEvent.Type.UPDATED) {
					this.updatedStarted.countDown();
					if (!inline) {
						assertTrue(this.releaseUpdated.await(30, TimeUnit.SECONDS), "UPDATED callback gate was not released");
					}
				}
				this.asyncMethodEvents.add(new EventInfo(event.getType(), inline));
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				this.callbackFailures.add(ex);
			}
			catch (Throwable ex) {
				this.callbackFailures.add(ex);
			}
			finally {
				if (event.getType() == EntryEvent.Type.UPDATED) {
					this.updatedCompleted.countDown();
				}
				this.asyncCallbacksCompleted.countDown();
			}
		}

		private void assertNoCallbackFailures() {
			Throwable failure = this.callbackFailures.peek();
			if (failure != null) {
				throw new AssertionError("Asynchronous observer callback failed", failure);
			}
		}

		private boolean isInline() {
			// Inline notification retains the dispatching onEvent frame on this stack.
			// Offloaded notification does not, regardless of executor or thread name.
			return StackWalker.getInstance().walk((frames) -> frames.anyMatch((frame) ->
					frame.getClassName().equals(EventHandler.class.getName()) && frame.getMethodName().equals("onEvent")));
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
