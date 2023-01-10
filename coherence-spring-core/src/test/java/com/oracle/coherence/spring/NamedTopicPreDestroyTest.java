/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import java.util.concurrent.atomic.AtomicBoolean;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.Coherence;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NamedTopicPreDestroyTest {

	AnnotationConfigApplicationContext ctx;

	@BeforeAll
	void beforeTestClass() {
		this.ctx = new AnnotationConfigApplicationContext(Config.class);
	}

	@Test
	void shouldClosePublisherOnScopeDeactivation() throws Exception {
		Publishers publishers = this.ctx.getBean(Publishers.class);
		Publisher<String> publisher = publishers.getPublisher();
		Publisher<String> qualifiedPublisher = publishers.getQualifiedPublisher();

		AtomicBoolean publisherClosed = new AtomicBoolean(false);
		AtomicBoolean qualifiedClosed = new AtomicBoolean(false);

		Subscribers subscribers = this.ctx.getBean(Subscribers.class);
		Subscriber<String> subscriber = subscribers.getSubscriber();
		Subscriber<String> qualifiedSubscriber = subscribers.getQualifiedSubscriber();

		assertThat(subscriber.isActive(), is(true));
		assertThat(qualifiedSubscriber.isActive(), is(true));

		publisher.onClose(() -> publisherClosed.set(true));
		qualifiedPublisher.onClose(() -> qualifiedClosed.set(true));

		this.ctx.close();

		assertThat(publisherClosed.get(), is(true));
		assertThat(qualifiedClosed.get(), is(true));
		assertThat(subscriber.isActive(), is(false));
		assertThat(qualifiedSubscriber.isActive(), is(false));
	}

	@AfterAll
	void afterTestClass() {
		this.ctx.close();
	}

	// ----- test beans -----------------------------------------------------

	@Singleton
	static class Subscribers {
		@Inject
		private Subscriber<String> numbers;

		@Inject
		@Name("numbers")
		private Subscriber<String> qualifiedSubscriber;

		Subscriber<String> getQualifiedSubscriber() {
			return this.qualifiedSubscriber;
		}

		Subscriber<String> getSubscriber() {
			return this.numbers;
		}
	}

	@Singleton
	static class Publishers {
		@Inject
		private Publisher<String> numbers;

		@Inject
		@Name("numbers")
		private Publisher<String> qualifiedPublisher;

		Publisher<String> getPublisher() {
			return this.numbers;
		}

		Publisher<String> getQualifiedPublisher() {
			return this.qualifiedPublisher;
		}
	}

	@Configuration
	@EnableCoherence
	static class Config {
		@Bean
		SessionConfigurationBean defaultSessionConfigurationBean() {
			final SessionConfigurationBean sessionConfigurationBean = new SessionConfigurationBean();
			sessionConfigurationBean.setName(Coherence.DEFAULT_NAME);
			sessionConfigurationBean.setConfig("coherence-cache-config.xml");
			sessionConfigurationBean.setType(SessionType.SERVER);
			return sessionConfigurationBean;
		}

		@Bean
		Publishers getPublishers() {
			return new Publishers();
		}

		@Bean
		Subscribers getSubscribers() {
			return new Subscribers();
		}
	}
}

