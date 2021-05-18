/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.coherence.spring.annotation.CoherenceTopicListener;
import com.oracle.coherence.spring.annotation.CommitStrategy;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.SubscriberGroup;
import com.oracle.coherence.spring.annotation.Topic;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.internal.net.topic.impl.paged.PagedTopicCaches;
import com.tangosol.internal.net.topic.impl.paged.model.SubscriberGroupId;
import com.tangosol.net.CacheService;
import com.tangosol.net.Coherence;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import data.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

@SpringJUnitConfig(CoherenceTopicListenerTest.Config.class)
@DirtiesContext
class CoherenceTopicListenerTest {

	@Inject
	Coherence coherence;

	@Inject
	ListenerOne listenerOne;

	@Inject
	ListenerTwo listenerTwo;

	@Inject
	ListenerThree listenerThree;

	@Inject
	ListenerFour listenerFour;

	@Inject
	ListenerFive listenerFive;

	@Inject
	ListenerSix listenerSix;

	@Autowired
	CoherenceTopicListenerSubscribers processor;

	@BeforeEach
	void setup() {
		// ensure that all subscriber methods are subscribed before the tests start
		// as subscription is async
		Eventually.assertDeferred(() -> this.processor.isSubscribed(), is(true));
	}

	@Test
	void shouldHaveSubscribed() throws Exception {
		String message = "message one";

		try (Publisher<String> publisher = getPublisher("One")) {
			publisher.publish(message);

			assertThat(this.listenerOne.latchOne.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerOne.messageOne, is(message));

			assertThat(this.listenerTwo.latchOne.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerTwo.messageOne, is(message));
		}
	}

	@Test
	void shouldHaveSubscribedWithGroups() throws Exception {
		NamedTopic<String> topic = this.coherence.getSession().getTopic("Two");
		assertThat(topic.getSubscriberGroups(), containsInAnyOrder("Foo", "Bar"));

		String message = "message two";
		try (Publisher<String> publisher = topic.createPublisher()) {
			publisher.publish(message);
			assertThat(this.listenerOne.latchTwoFoo.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerOne.messageTwoFoo, is(message));
			assertThat(this.listenerOne.latchTwoBar.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerOne.messageTwoBar, is(message));
		}
	}

	@Test
	void shouldHaveSubscribedWithFilter() throws Exception {
		try (Publisher<Person> publisher = getPublisher("Three")) {
			Person homer = new Person("Homer", "Simpson", LocalDate.now(), null);
			publisher.publish(new Person("Ned", "Flanders", LocalDate.now(), null));
			publisher.publish(homer);
			publisher.publish(new Person("Apu", "Nahasapeemapetilon", LocalDate.now(), null));

			assertThat(this.listenerOne.latchThree.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerOne.messageThree, is(notNullValue()));
			assertThat(this.listenerOne.messageThree, is(homer));
		}
	}

	@Test
	void shouldHaveSubscribedWithConverter() throws Exception {
		try (Publisher<Person> publisher = getPublisher("People")) {
			publisher.publish(new Person("Homer", "Simpson", LocalDate.now(), null));

			assertThat(this.listenerOne.latchPeopleConverted.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerOne.messagePeopleConverted, is(notNullValue()));
			assertThat(this.listenerOne.messagePeopleConverted, is("Homer"));
		}
	}

	@Test
	void shouldHaveSubscribedToTopicFromMethodName() throws Exception {
		try (Publisher<String> publisher = getPublisher("four")) {
			String message = "message four";
			publisher.publish(message);

			assertThat(this.listenerOne.latchFour.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerOne.messageFour, is(message));

			assertThat(this.listenerTwo.latchFour.await(1, TimeUnit.MINUTES), is(true));
			assertThat(this.listenerTwo.messageFour, is(message));
		}
	}

	@Test
	void shouldSendListenerResultToTargetTopic() throws Exception {
		try (Publisher<String> publisher = getPublisher("Five");
				Subscriber<String> subscriber = getSubscriber("Six")) {

			CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

			String message = "message five";
			publisher.publish(message).join();

			Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is(message.toUpperCase()));
		}
	}

	@Test
	void shouldNotSendVoidResultToTargetTopic() throws Exception {
		try (Publisher<String> publisher = getPublisher("Seven");
				Subscriber<String> subscriber = getSubscriber("Eight")) {

			CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

			String message = "message seven";
			publisher.publish(message).get(1, TimeUnit.MINUTES);

			getPublisher("Eight").publish("No Message").get(1, TimeUnit.MINUTES);
			Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is("No Message"));
		}
	}

	@Test
	void shouldSendListenerResultToMultipleTargetTopics() throws Exception {
		try (Publisher<String> publisher = getPublisher("Nine");
				Subscriber<String> subscriber1 = getSubscriber("Ten");
				Subscriber<String> subscriber2 = getSubscriber("Eleven")) {

			CompletableFuture<Subscriber.Element<String>> future1 = subscriber1.receive();
			CompletableFuture<Subscriber.Element<String>> future2 = subscriber2.receive();

			String message = "message nine";
			publisher.publish(message);

			Subscriber.Element<String> element = future1.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is(message.toUpperCase()));

			element = future2.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is(message.toUpperCase()));
		}
	}

	@Test
	void shouldSendListenerAsyncResultToTargetTopic() throws Exception {
		try (Publisher<String> publisher = getPublisher("Twelve");
				Subscriber<String> subscriber = getSubscriber("Thirteen")) {

			CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

			String message = "message twelve";
			publisher.publish(message);

			Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is(message.toUpperCase()));
		}
	}

	@Test
	void shouldSendListenerSingleReactiveResultToTargetTopic() throws Exception {
		try (Publisher<String> publisher = getPublisher("Fourteen");
				Subscriber<String> subscriber = getSubscriber("Fifteen")) {

			CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

			String message = "message fourteen";
			publisher.publish(message);

			Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is(message.toUpperCase()));
		}
	}

	@Test
	void shouldSendListenerReactiveResultToTargetTopic() throws Exception {
		try (Publisher<String> publisher = getPublisher("Sixteen");
				Subscriber<Character> subscriber = getSubscriber("Seventeen")) {

			CompletableFuture<Subscriber.Element<Character>> future = subscriber.receive();

			String message = "ABC";
			publisher.publish(message);

			Subscriber.Element<Character> element = future.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is('A'));

			future = subscriber.receive();
			element = future.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is('B'));

			future = subscriber.receive();
			element = future.get(1, TimeUnit.MINUTES);
			assertThat(element, is(notNullValue()));
			assertThat(element.getValue(), is('C'));
		}
	}

	@Test
	void shouldReceiveElementBinderArguments() throws Exception {
		int channel = 1;
		try (Publisher<String> publisher = getPublisher("Eighteen", Publisher.OrderBy.id(channel))) {
			String message = "ABC";
			Publisher.Status status = publisher.publish(message).get(1, TimeUnit.MINUTES);

			this.listenerFour.latch.await(1, TimeUnit.MINUTES);
			assertThat(this.listenerFour.lastElementOne, is(notNullValue()));
			assertThat(this.listenerFour.lastElementOne.getChannel(), is(status.getChannel()));
			assertThat(this.listenerFour.lastElementOne.getPosition(), is(status.getPosition()));
			assertThat(this.listenerFour.lastElementOne.getValue(), is(message));
			assertThat(this.listenerFour.lastElementTwo, is(notNullValue()));
			assertThat(this.listenerFour.lastElementTwo.getChannel(), is(status.getChannel()));
			assertThat(this.listenerFour.lastElementTwo.getPosition(), is(status.getPosition()));
			assertThat(this.listenerFour.lastElementTwo.getValue(), is(message));
			assertThat(this.listenerFour.lastValueThree, is(message));
			assertThat(this.listenerFour.lastValueFour, is(message));
			assertThat(this.listenerFour.lastValueFive, is(message));
		}
	}


	@Test
	void shouldReceiveMessagesForMultipleSubscribersInSameGroup() {
		AtomicInteger count = new AtomicInteger();

		try (Publisher<String> publisher = getPublisher("Nineteen", Publisher.OrderByValue.value((v) -> count.getAndIncrement()))) {
			for (int i = 0; i < publisher.getChannelCount() * 2; i++) {
				publisher.publish("message-" + i);
			}

			int expected = count.get();
			Eventually.assertDeferred(() -> this.listenerFive.received(), is(expected));
			TreeSet<String> set = new TreeSet<>(this.listenerFive.listOne);
			set.addAll(this.listenerFive.listTwo);
			assertThat(set.size(), is(expected));
		}
	}

	@Test
	void shouldCommitWithDefaultStrategy() throws Exception {
		NamedTopic<String> topic = this.coherence.getSession().getTopic("TwentyDefault");
		PagedTopicCaches caches = new PagedTopicCaches(topic.getName(), (CacheService) topic.getService());
		SubscriberGroupId groupId = SubscriberGroupId.withName(ListenerSix.GROUP_ID);

		try (Publisher<String> publisher = topic.createPublisher()) {
			for (int i = 0; i < publisher.getChannelCount(); i++) {
				publisher.publish("test").get(1, TimeUnit.MINUTES);
			}

			Eventually.assertDeferred(() -> this.listenerSix.countDefault.get(), is(not(0)));
			Eventually.assertDeferred(() -> caches.isCommitted(groupId, this.listenerSix.element.getChannel(), this.listenerSix.element.getPosition()), is(true));
		}
	}

	@Test
	void shouldCommitWithSyncStrategy() throws Exception {
		NamedTopic<String> topic = this.coherence.getSession().getTopic("TwentySync");
		PagedTopicCaches caches = new PagedTopicCaches(topic.getName(), (CacheService) topic.getService());
		SubscriberGroupId groupId = SubscriberGroupId.withName(ListenerSix.GROUP_ID);

		try (Publisher<String> publisher = topic.createPublisher()) {
			for (int i = 0; i < publisher.getChannelCount(); i++) {
				publisher.publish("test").get(1, TimeUnit.MINUTES);
			}

			Eventually.assertDeferred(() -> this.listenerSix.countSync.get(), is(not(0)));
			Eventually.assertDeferred(() -> caches.isCommitted(groupId, this.listenerSix.element.getChannel(), this.listenerSix.element.getPosition()), is(true));
		}
	}

	@Test
	void shouldCommitWithAsyncStrategy() {
		NamedTopic<String> topic = this.coherence.getSession().getTopic("TwentyAsync");
		PagedTopicCaches caches = new PagedTopicCaches(topic.getName(), (CacheService) topic.getService());
		SubscriberGroupId groupId = SubscriberGroupId.withName(ListenerSix.GROUP_ID);

		try (Publisher<String> publisher = topic.createPublisher()) {
			for (int i = 0; i < publisher.getChannelCount(); i++) {
				publisher.publish("test").join();
			}

			Eventually.assertDeferred(() -> this.listenerSix.countAsync.get(), is(not(0)));
			Eventually.assertDeferred(() -> caches.isCommitted(groupId, this.listenerSix.element.getChannel(), this.listenerSix.element.getPosition()), is(true));
		}
	}

	@Test
	void shouldCommitWithManualStrategy() throws Exception {
		NamedTopic<String> topic = this.coherence.getSession().getTopic("TwentyManual");
		PagedTopicCaches caches = new PagedTopicCaches(topic.getName(), (CacheService) topic.getService());
		SubscriberGroupId groupId = SubscriberGroupId.withName(ListenerSix.GROUP_ID);

		try (Publisher<String> publisher = topic.createPublisher()) {
			// publish four messages
			for (int i = 0; i < 4; i++) {
				publisher.publish("element-" + i).get(1, TimeUnit.MINUTES);
			}
			// should receive four messages
			Eventually.assertDeferred(() -> this.listenerSix.countManual.get(), is(4));
			// fourth message should not be committed
			assertThat(caches.isCommitted(groupId, this.listenerSix.element.getChannel(), this.listenerSix.element.getPosition()), is(false));
			// publish a fifth message
			publisher.publish("element-5").get(1, TimeUnit.MINUTES);
			// should receive fifth messages
			Eventually.assertDeferred(() -> this.listenerSix.countManual.get(), is(5));
			// fifth message should not be committed
			assertThat(caches.isCommitted(groupId, this.listenerSix.element.getChannel(), this.listenerSix.element.getPosition()), is(true));
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Publisher<T> getPublisher(String name, Publisher.Option... options) {
		NamedTopic<String> topic = this.coherence.getSession().getTopic(name);
		return (Publisher<T>) topic.createPublisher(options);
	}

	@SuppressWarnings("unchecked")
	private <T> Subscriber<T> getSubscriber(String name) {
		NamedTopic<String> topic = this.coherence.getSession().getTopic(name);
		return (Subscriber<T>) topic.createSubscriber();
	}

	@CoherenceTopicListener
	static class ListenerOne {
		private final CountDownLatch latchOne = new CountDownLatch(1);
		private final CountDownLatch latchTwoFoo = new CountDownLatch(1);
		private final CountDownLatch latchTwoBar = new CountDownLatch(1);
		private final CountDownLatch latchThree = new CountDownLatch(1);
		private final CountDownLatch latchPeopleConverted = new CountDownLatch(1);
		private final CountDownLatch latchFour = new CountDownLatch(1);
		private String messageOne;
		private String messageTwoFoo;
		private String messageTwoBar;
		private Person messageThree;
		private String messagePeopleConverted;
		private String messageFour;

		@Topic("One")
		void listenOne(String value) {
			this.messageOne = value;
			this.latchOne.countDown();
		}

		@Topic("Two")
		@SubscriberGroup("Foo")
		void listenTwoFoo(String value) {
			this.messageTwoFoo = value;
			this.latchTwoFoo.countDown();
		}

		@Topic("Two")
		@SubscriberGroup("Bar")
		void listenTwoBar(String value) {
			this.messageTwoBar = value;
			this.latchTwoBar.countDown();
		}

		@Topic("Three")
		@WhereFilter("lastName == 'Simpson'")
		void listenThree(Person value) {
			this.messageThree = value;
			this.latchThree.countDown();
		}

		@Topic("People")
		@PropertyExtractor("firstName")
		void listenThreeConverted(String value) {
			this.messagePeopleConverted = value;
			this.latchPeopleConverted.countDown();
		}

		/**
		 * Receives messages sent to topic "four"
		 */
		@SuppressWarnings("unused")
		void four(String value) {
			this.messageFour = value;
			this.latchFour.countDown();
		}
	}

	@Singleton
	static class ListenerTwo {
		private final CountDownLatch latchOne = new CountDownLatch(1);
		private final CountDownLatch latchFour = new CountDownLatch(1);
		private String messageOne;
		private String messageFour;

		@Topic("One")
		@CoherenceTopicListener
		void listenOne(String value) {
			this.messageOne = value;
			this.latchOne.countDown();
		}

		@CoherenceTopicListener
		void four(String value) {
			this.messageFour = value;
			this.latchFour.countDown();
		}
	}

	@Singleton
	static class ListenerThree {
		@Topic("Five")
		@SendTo("Six")
		@CoherenceTopicListener
		String toUpper(String value) {
			return value.toUpperCase();
		}

		@Topic("Seven")
		@SendTo("Eight")
		@CoherenceTopicListener
		void noSendTo(String value) {
		}

		@Topic("Nine")
		@SendTo({"Ten", "Eleven"})
		@CoherenceTopicListener
		String multiSendTo(String value) {
			return value.toUpperCase();
		}

		@Topic("Twelve")
		@SendTo("Thirteen")
		@CoherenceTopicListener
		CompletableFuture<String> asyncSendTo(String value) {
			return CompletableFuture.supplyAsync(value::toUpperCase);
		}

		@Topic("Fourteen")
		@SendTo("Fifteen")
		@CoherenceTopicListener
		Mono<String> reactiveSingleSendTo(String value) {
			return Mono.fromFuture(CompletableFuture.supplyAsync(value::toUpperCase));
		}

		@Topic("Sixteen")
		@SendTo("Seventeen")
		@CoherenceTopicListener
		Flux<Character> reactiveSendTo(String value) {
			List<Character> list = new ArrayList<>();
			for (char c : value.toCharArray()) {
				list.add(c);
			}
			return Flux.fromArray(list.toArray(new Character[0]));
		}
	}

	@Singleton
	static class ListenerFour {

		CountDownLatch latch = new CountDownLatch(6);
		Subscriber.Element<String> lastElementOne;
		Subscriber.Element<String> lastElementTwo;
		String lastValueThree;
		String lastValueFour;
		String lastValueFive;

		@Topic("Eighteen")
		@SubscriberGroup("One")
		@CoherenceTopicListener
		void element(Subscriber.Element<String> element) {
			this.lastElementOne = element;
			this.latch.countDown();
		}

		@Topic("Eighteen")
		@SubscriberGroup("Two")
		@CoherenceTopicListener
		void elementTwo(Subscriber.Element<String> element) {
			this.lastElementTwo = element;
			this.latch.countDown();
		}

		@Topic("Eighteen")
		@SubscriberGroup("Three")
		@CoherenceTopicListener
		void value(String s) {
			this.lastValueThree = s;
			this.latch.countDown();
		}

		@Topic("Eighteen")
		@SubscriberGroup("Four")
		@CoherenceTopicListener
		void valueFour(String s) {
			this.lastValueFour = s;
			this.latch.countDown();
		}

		@Topic("Eighteen")
		@SubscriberGroup("Five")
		@CoherenceTopicListener
		void valueFive(String s) {
			this.lastValueFive = s;
			this.latch.countDown();
		}

		@Topic("Eighteen")
		@SubscriberGroup("Six")
		@CoherenceTopicListener
		void valueSix(String s) {
			this.latch.countDown();
		}
	}

	@Singleton
	static class ListenerFive {

		private final List<String> listOne = new ArrayList<>();
		private final  List<String> listTwo = new ArrayList<>();
		private final  AtomicInteger count = new AtomicInteger();

		@Topic("Nineteen")
		@SubscriberGroup("test")
		@CoherenceTopicListener
		void one(String value) {
			this.listOne.add(value);
			this.count.incrementAndGet();
		}

		@Topic("Nineteen")
		@SubscriberGroup("test")
		@CoherenceTopicListener
		void two(String value) {
			this.listTwo.add(value);
			this.count.incrementAndGet();
		}

		int received() {
			return this.count.get();
		}
	}

	@Singleton
	static class ListenerSix {
		public static final String GROUP_ID = "test";

		private final AtomicInteger countManual = new AtomicInteger();
		private final AtomicInteger countAsync = new AtomicInteger();
		private final AtomicInteger countSync = new AtomicInteger();
		private final AtomicInteger countDefault = new AtomicInteger();

		private volatile Subscriber.Element<String> element;

		@Topic("TwentyManual")
		@SubscriberGroup(GROUP_ID)
		@CoherenceTopicListener(commitStrategy = CommitStrategy.MANUAL)
		void one(Subscriber.Element<String> e) {
			this.element = e;
			// manually commit the fifth message
			if (this.countManual.get() == 4) {
				e.commit();
			}
			this.countManual.incrementAndGet();
		}

		@Topic("TwentyAsync")
		@SubscriberGroup(GROUP_ID)
		@CoherenceTopicListener(commitStrategy = CommitStrategy.ASYNC)
		void two(Subscriber.Element<String> e) {
			this.element = e;
			this.countAsync.incrementAndGet();
		}

		@Topic("TwentySync")
		@SubscriberGroup(GROUP_ID)
		@CoherenceTopicListener(commitStrategy = CommitStrategy.SYNC)
		void three(Subscriber.Element<String> e) {
			this.element = e;
			this.countSync.incrementAndGet();
		}

		@Topic("TwentyDefault")
		@SubscriberGroup(GROUP_ID)
		@CoherenceTopicListener
		void four(Subscriber.Element<String> e) {
			this.element = e;
			this.countDefault.incrementAndGet();
		}
	}

	@Configuration
	@EnableCoherence
	@EnableCaching
	static class Config {
		@Bean
		ListenerOne getListenerOne() {
			return new ListenerOne();
		}

		@Bean
		ListenerTwo getListenerTw() {
			return new ListenerTwo();
		}

		@Bean
		ListenerThree getListenerThree() {
			return new ListenerThree();
		}

		@Bean
		ListenerFour getListenerFour() {
			return new ListenerFour();
		}

		@Bean
		ListenerFive getListenerFive() {
			return new ListenerFive();
		}

		@Bean
		ListenerSix getListenerSix() {
			return new ListenerSix();
		}
	}

}

