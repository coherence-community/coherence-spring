/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.oracle.coherence.spring.annotation.CoherencePublisher;
import com.oracle.coherence.spring.annotation.CoherencePublisherScan;
import com.oracle.coherence.spring.annotation.Topic;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.net.Coherence;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringJUnitConfig(CoherencePublisherTest.Config.class)
@DirtiesContext
public class CoherencePublisherTest {

	@Autowired
	Coherence coherence;

	@Autowired
	PublishersOne publishersOne;

	@Test
	void shouldSendMessage() throws Exception {
		Subscriber<String> subscriber = getSubscriber("One");
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing One...";
		this.publishersOne.send(message);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendMessageToNamedTopic() throws Exception {
		String topicName = "OneTwo";
		Subscriber<String> subscriber = getSubscriber(topicName);
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing named topic...";
		this.publishersOne.sendTo(topicName, message);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendMessageWithAsyncResponse() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Two");
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing Two...";
		CompletableFuture<Publisher.Status> sent = this.publishersOne.sendAsync(message);

		Publisher.Status status = sent.get(1, TimeUnit.MINUTES);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendMessageWithReactiveResponse() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Three");
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing Two...";
		Mono<Publisher.Status> sent = this.publishersOne.sendWithReactiveResponse(message);

		Publisher.Status status = sent.toFuture().get(1, TimeUnit.MINUTES);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendSingleReactiveMessages() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Four");
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing Reactive...";
		Flux<String> observable = Flux.fromArray(new String[]{message});
		this.publishersOne.sendReactive(observable);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendAllReactiveMessages() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Four");
		CompletableFuture<Subscriber.Element<String>> future;

		future = subscriber.receive();

		Flux<String> observable = Flux.fromArray(new String[]{"One", "Two", "Three"});
		this.publishersOne.sendReactive(observable);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("One"));

		future = subscriber.receive();
		element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("Two"));

		future = subscriber.receive();
		element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("Three"));
	}

	@Test
	void shouldSendSingleReactiveMessagesWithAsyncResponse() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Five");
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing Reactive...";
		Flux<String> observable = Flux.fromArray(new String[]{message});
		CompletableFuture<List<Publisher.Status>> sendFuture = this.publishersOne.sendReactiveAsync(observable);

		List<Publisher.Status> statuses = sendFuture.get(1, TimeUnit.MINUTES);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendSingleReactiveMessagesWithAsyncResponse2() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Five");
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing Reactive...";
		Mono<String> observable = Mono.just(message);
		CompletableFuture<Publisher.Status> sendFuture = this.publishersOne.sendReactiveAsync(observable);

		Publisher.Status status = sendFuture.get(1, TimeUnit.MINUTES);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendAllReactiveMessagesWithAsyncResponse() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Five");
		CompletableFuture<Subscriber.Element<String>> future;

		future = subscriber.receive();

		Flux<String> observable = Flux.fromArray(new String[]{"One", "Two", "Three"});
		CompletableFuture<List<Publisher.Status>> sendFuture = this.publishersOne.sendReactiveAsync(observable);

		List<Publisher.Status> statuses = sendFuture.get(1, TimeUnit.MINUTES);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("One"));

		future = subscriber.receive();
		element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("Two"));

		future = subscriber.receive();
		element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("Three"));
	}

	@Test
	void shouldSendSingleReactiveMessagesWithReactiveResponse() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Six");
		CompletableFuture<Subscriber.Element<String>> future = subscriber.receive();

		String message = "Testing Reactive...";
		Flux<String> observable = Flux.fromArray(new String[]{message});
		Flux<Publisher.Status> sent = this.publishersOne.sendReactiveWithReactiveResponse(observable);

		Publisher.Status status = sent.single().toFuture().get(1, TimeUnit.MINUTES);

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is(message));
	}

	@Test
	void shouldSendAllReactiveMessagesWithReactiveResponse() throws Exception {
		Subscriber<String> subscriber = getSubscriber("Six");
		CompletableFuture<Subscriber.Element<String>> future;

		future = subscriber.receive();

		Flux<String> observable = Flux.fromArray(new String[]{"One", "Two", "Three"});
		Flux<Publisher.Status> sent = this.publishersOne.sendReactiveWithReactiveResponse(observable);
		sent.subscribe();

		Subscriber.Element<String> element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("One"));

		future = subscriber.receive();
		element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("Two"));

		future = subscriber.receive();
		element = future.get(1, TimeUnit.MINUTES);
		assertThat(element, is(notNullValue()));
		assertThat(element.getValue(), is("Three"));
	}

	private Subscriber<String> getSubscriber(String name) {
		NamedTopic<String> topic = this.coherence.getSession().getTopic(name);
		return topic.createSubscriber();
	}

	@Configuration
	@EnableCoherence
	@EnableCaching
	@CoherencePublisherScan("com.oracle.coherence")
	static class Config {
	}

	@Singleton
	@CoherencePublisher(maxBlock = "PT15M", proxyDefaultMethods = true)
	interface PublishersOne {
		@Topic("One")
		default void send(String message) {
		}

		void sendTo(@Topic("One") String topic, String message);

		@Topic("Two")
		CompletableFuture<Publisher.Status> sendAsync(String message);

		@Topic("Three")
		Mono<Publisher.Status> sendWithReactiveResponse(String message);

		@Topic("Four")
		void sendReactive(Flux<String> observable);

		@Topic("Five")
		CompletableFuture<List<Publisher.Status>> sendReactiveAsync(Flux<String> observable);

		@Topic("Five")
		CompletableFuture<Publisher.Status> sendReactiveAsync(Mono<String> observable);

		@Topic("Six")
		Flux<Publisher.Status> sendReactiveWithReactiveResponse(Flux<String> observable);
	}

}
