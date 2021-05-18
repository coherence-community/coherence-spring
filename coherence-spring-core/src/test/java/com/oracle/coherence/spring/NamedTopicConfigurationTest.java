/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.tangosol.net.Coherence;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import data.Person;
import data.PhoneNumber;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;


@SpringJUnitConfig(NamedTopicConfigurationTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NamedTopicConfigurationTest {
	@Inject
	ApplicationContext ctx;

	@Test
	void shouldInjectNamedTopicUsingFieldName() {
		NamedTopicFieldsBean bean = this.ctx.getBean(NamedTopicFieldsBean.class);
		assertThat(bean.getNumbers(), is(notNullValue()));
		assertThat(bean.getNumbers().getName(), is("numbers"));
	}

	@Test
	void shouldInjectNamedTopicWithGenericValues() {
		NamedTopicFieldsBean bean = this.ctx.getBean(NamedTopicFieldsBean.class);
		assertThat(bean.getGenericValues(), is(notNullValue()));
		assertThat(bean.getGenericValues().getName(), is("genericValues"));
	}


	@Test
	void shouldInjectNamedTopicWithGenerics() {
		NamedTopicFieldsBean bean = this.ctx.getBean(NamedTopicFieldsBean.class);
		assertThat(bean.getGenericTopic(), is(notNullValue()));
		assertThat(bean.getGenericTopic().getName(), is("numbers"));
	}

	@Test
	void shouldInjectQualifiedNamedTopic() {
		NamedTopicFieldsBean bean = this.ctx.getBean(NamedTopicFieldsBean.class);
		assertThat(bean.getNamedTopic(), is(notNullValue()));
		assertThat(bean.getNamedTopic().getName(), is("numbers"));
	}

	@Test
	void shouldInjectTopicsFromDifferentSessions() throws Exception {
		DifferentSessionBean bean = this.ctx.getBean(DifferentSessionBean.class);

		NamedTopic<String> defaultTopic = bean.getDefaultCcfNumbers();
		NamedTopic<String> specificTopic = bean.getSpecificCcfNumbers();

		assertThat(defaultTopic, is(notNullValue()));
		assertThat(defaultTopic.getName(), Matchers.is("numbers"));

		assertThat(specificTopic, is(notNullValue()));
		assertThat(specificTopic.getName(), Matchers.is("numbers"));

		assertThat(defaultTopic, is(not(sameInstance(specificTopic))));

		Subscriber<String> defaultSubscriber = bean.getDefaultSubscriber();
		CompletableFuture<Subscriber.Element<String>> defaultFuture = defaultSubscriber.receive();
		Subscriber<String> specificSubscriber = bean.getSpecificSubscriber();
		CompletableFuture<Subscriber.Element<String>> specificFuture = specificSubscriber.receive();

		bean.getDefaultPublisher().publish("value-one");
		bean.getSpecificPublisher().publish("value-two");

		Subscriber.Element<String> valueOne = defaultFuture.get(1, TimeUnit.MINUTES);
		Subscriber.Element<String> valueTwo = specificFuture.get(1, TimeUnit.MINUTES);

		assertThat(valueOne.getValue(), is("value-one"));
		assertThat(valueTwo.getValue(), is("value-two"));
	}

	@Test
	void shouldInjectIntoConstructor() {
		CtorBean bean = this.ctx.getBean(CtorBean.class);

		assertThat(bean.getNumbers(), Matchers.notNullValue());
		assertThat(bean.getNumbers().getName(), Matchers.is("numbers"));
	}

	@Test
	public void shouldInjectPublisher() throws Exception {
		NamedTopicPublisherFieldsBean publisherBean = this.ctx.getBean(NamedTopicPublisherFieldsBean.class);
		NamedTopicSubscriberFieldsBean subscriberBean = this.ctx.getBean(NamedTopicSubscriberFieldsBean.class);

		Publisher<Integer> numbersPublisher = publisherBean.getNumbers();
		assertThat(numbersPublisher, is(notNullValue()));

		Publisher<Person> peoplePublisher = publisherBean.getPeople();
		assertThat(peoplePublisher, is(notNullValue()));

		Subscriber<Integer> numbersSubscriber = subscriberBean.getNumbers();
		assertThat(numbersSubscriber, is(notNullValue()));

		Subscriber<Person> peopleSubscriber = subscriberBean.getPeople();
		assertThat(peopleSubscriber, is(notNullValue()));

		Subscriber<String> peopleFirstNamesSubscriber = subscriberBean.getPeopleFirstNames();
		assertThat(peopleFirstNamesSubscriber, is(notNullValue()));

		Subscriber<Person> peopleFilteredSubscriber = subscriberBean.getPeopleFiltered();
		assertThat(peopleFilteredSubscriber, is(notNullValue()));

		CompletableFuture<Subscriber.Element<Integer>> receiveNumber = numbersSubscriber.receive();
		numbersPublisher.publish(19).join();
		Subscriber.Element<Integer> element = receiveNumber.get(1, TimeUnit.MINUTES);
		assertThat(element.getValue(), is(19));

		Person homer = new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999"));
		Person bart = new Person("Bart", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999"));

		CompletableFuture<Subscriber.Element<Person>> receivePerson = peopleSubscriber.receive();
		CompletableFuture<Subscriber.Element<String>> receiveName = peopleFirstNamesSubscriber.receive();
		CompletableFuture<Subscriber.Element<Person>> receiveFiltered = peopleFilteredSubscriber.receive();

		peoplePublisher.publish(homer).join();

		Subscriber.Element<Person> personElement = receivePerson.get(1, TimeUnit.MINUTES);
		Subscriber.Element<String> nameElement = receiveName.get(1, TimeUnit.MINUTES);
		assertThat(personElement.getValue(), is(homer));
		assertThat(nameElement.getValue(), is(homer.getFirstName()));

		assertThat(receiveFiltered.isDone(), is(false));
		peoplePublisher.publish(bart).join();
		personElement = receiveFiltered.get(1, TimeUnit.MINUTES);
		assertThat(personElement.getValue(), is(bart));
	}

	@Configuration
	@EnableCoherence
	static class Config {
		@Bean
		SessionConfigurationBean testSessionConfigurationBean() {
			final SessionConfigurationBean sessionConfigurationBean = new SessionConfigurationBean();
			sessionConfigurationBean.setName("test");
			sessionConfigurationBean.setConfig("test-coherence-config.xml");
			sessionConfigurationBean.setType(SessionType.SERVER);
			sessionConfigurationBean.setScopeName("Test");
			return sessionConfigurationBean;
		}

		@Bean
		SessionConfigurationBean defaultSessionConfigurationBean() {
			final SessionConfigurationBean sessionConfigurationBean = new SessionConfigurationBean();
			sessionConfigurationBean.setName(Coherence.DEFAULT_NAME);
			sessionConfigurationBean.setConfig("coherence-cache-config.xml");
			sessionConfigurationBean.setType(SessionType.SERVER);
			return sessionConfigurationBean;
		}

		@Bean
		NamedTopicFieldsBean namedTopicFieldsBean() {
			return new NamedTopicFieldsBean();
		}

		@Bean
		DifferentSessionBean differentSessionBean() {
			return new DifferentSessionBean();
		}

		@Bean
		NamedTopicPublisherFieldsBean namedTopicPublisherFieldsBean() {
			return new NamedTopicPublisherFieldsBean();
		}

		@Bean
		NamedTopicSubscriberFieldsBean namedTopicSubscriberFieldsBean() {
			return new NamedTopicSubscriberFieldsBean();
		}

		@Bean
		CtorBean ctorBean(@Value("#{getTopic}") @Name("numbers") NamedTopic<Integer> numbers) {
			return new CtorBean(numbers);
		}

	}

	// ----- test beans -----------------------------------------------------

	@Singleton
	static class NamedTopicFieldsBean {
		@Inject
		private NamedTopic<String> numbers;

		@Inject
		@Name("numbers")
		private NamedTopic<String> namedTopic;

		@Inject
		@Name("numbers")
		private NamedTopic<Integer> genericTopic;

		@Inject
		private NamedTopic<List<String>> genericValues;

		NamedTopic<Integer> getGenericTopic() {
			return this.genericTopic;
		}

		NamedTopic<List<String>> getGenericValues() {
			return this.genericValues;
		}

		NamedTopic<String> getNamedTopic() {
			return this.namedTopic;
		}

		NamedTopic<String> getNumbers() {
			return this.numbers;
		}
	}

	@Singleton
	static class NamedTopicPublisherFieldsBean {
		@Inject
		private Publisher<Person> people;

		@Inject
		@Name("numbers")
		private Publisher<Integer> numbersPublisher;

		Publisher<Integer> getNumbers() {
			return this.numbersPublisher;
		}

		Publisher<Person> getPeople() {
			return this.people;
		}
	}

	@Singleton
	static class NamedTopicSubscriberFieldsBean {
		@Inject
		private Subscriber<Person> people;

		@Inject
		@Name("numbers")
		private Subscriber<Integer> namedTopic;

		@Inject
		@Name("people")
		@PropertyExtractor("firstName")
		private Subscriber<String> peopleFirstNames;

		@Inject
		@Name("people")
		@WhereFilter("firstName == 'Bart'")
		private Subscriber<Person> peopleFiltered;

		Subscriber<Integer> getNumbers() {
			return this.namedTopic;
		}

		Subscriber<Person> getPeople() {
			return this.people;
		}

		Subscriber<String> getPeopleFirstNames() {
			return this.peopleFirstNames;
		}

		Subscriber<Person> getPeopleFiltered() {
			return this.peopleFiltered;
		}
	}

	@Singleton
	static class DifferentSessionBean {
		@Inject
		@Name("numbers")
		private NamedTopic<String> defaultCcfNumbers;

		@Inject
		@Name("numbers")
		private Publisher<String> defaultPublisher;

		@Inject
		@Name("numbers")
		private Subscriber<String> defaultSubscriber;

		@Inject
		@Name("numbers")
		@SessionName("test")
		private NamedTopic<String> specificCcfNumbers;

		@Inject
		@Name("numbers")
		@SessionName("test")
		private Publisher<String> specificPublisher;

		@Inject
		@Name("numbers")
		@SessionName("test")
		private Subscriber<String> specificSubscriber;

		NamedTopic<String> getDefaultCcfNumbers() {
			return this.defaultCcfNumbers;
		}

		Publisher<String> getDefaultPublisher() {
			return this.defaultPublisher;
		}

		Subscriber<String> getDefaultSubscriber() {
			return this.defaultSubscriber;
		}

		NamedTopic<String> getSpecificCcfNumbers() {
			return this.specificCcfNumbers;
		}

		Publisher<String> getSpecificPublisher() {
			return this.specificPublisher;
		}

		Subscriber<String> getSpecificSubscriber() {
			return this.specificSubscriber;
		}
	}


	static class CtorBean {
		private final NamedTopic<Integer> numbers;

		@Inject
		CtorBean(@Name("numbers") NamedTopic<Integer> topic) {
			this.numbers = topic;
		}

		NamedTopic<Integer> getNumbers() {
			return this.numbers;
		}
	}

}
