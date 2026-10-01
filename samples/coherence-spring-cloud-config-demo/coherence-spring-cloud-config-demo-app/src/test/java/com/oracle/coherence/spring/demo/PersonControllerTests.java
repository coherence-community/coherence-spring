/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;

import com.oracle.coherence.spring.demo.dao.EventRepository;
import com.oracle.coherence.spring.demo.dao.PersonRepository;
import com.oracle.coherence.spring.demo.model.Event;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.jpa.open-in-view=false", "spring.cloud.config.enabled=false" })
class PersonControllerTests {

	@LocalServerPort
	private int port;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private final HttpClient client = HttpClient.newHttpClient();

	@Test
	void shouldSerializePeopleAndLazyCollectionsAfterTransactionCloses() throws Exception {
		long firstId = createPerson("Ada", "Lovelace", 36);
		JsonNode empty = getPeople(0).path("content").get(0);
		assertThat(empty.path("id").asLong()).isEqualTo(firstId);
		assertThat(empty.path("emailAddresses").size()).isZero();
		assertThat(empty.path("events").size()).isZero();

		new TransactionTemplate(this.transactionManager).executeWithoutResult((status) -> {
			var person = this.personRepository.findById(firstId).orElseThrow();
			person.getEmailAddresses().add("ada@example.test");
			Event event = new Event();
			event.setTitle("Analytical Engine");
			event.setDate(new Date(0));
			this.eventRepository.save(event);
			person.addToEvent(event);
		});
		long secondId = createPerson("Grace", "Hopper", 85);

		JsonNode page = getPeople(0);
		assertThat(page.path("totalElements").asLong()).isEqualTo(2);
		assertThat(page.path("totalPages").asInt()).isEqualTo(2);
		assertThat(page.path("content").size()).isEqualTo(1);
		JsonNode person = page.path("content").get(0);
		assertThat(person.path("firstname").asString()).isEqualTo("Ada");
		assertThat(person.path("lastname").asString()).isEqualTo("Lovelace");
		assertThat(person.path("age").asInt()).isEqualTo(36);
		assertThat(person.path("emailAddresses").get(0).asString()).isEqualTo("ada@example.test");
		assertThat(person.path("events").size()).isEqualTo(1);
		JsonNode event = person.path("events").get(0);
		assertThat(event.path("id").asLong()).isPositive();
		assertThat(event.path("title").asString()).isEqualTo("Analytical Engine");
		assertThat(event.path("date").isMissingNode()).isFalse();
		assertThat(event.path("date").isNull()).isFalse();
		assertThat(event.has("participants")).isFalse();
		assertThat(getPeople(1).path("content").get(0).path("id").asLong()).isEqualTo(secondId);
	}

	private long createPerson(String firstName, String lastName, int age) throws Exception {
		URI uri = URI.create("http://localhost:" + this.port + "/api/people?firstName=" + firstName
				+ "&lastName=" + lastName + "&age=" + age);
		HttpResponse<String> response = this.client.send(
				HttpRequest.newBuilder(uri).POST(HttpRequest.BodyPublishers.noBody()).build(),
				HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
		return this.objectMapper.readTree(response.body()).asLong();
	}

	private JsonNode getPeople(int page) throws Exception {
		URI uri = URI.create("http://localhost:" + this.port + "/api/people?page=" + page + "&size=1&sort=id,asc");
		HttpResponse<String> response = this.client.send(HttpRequest.newBuilder(uri).GET().build(),
				HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
		return this.objectMapper.readTree(response.body());
	}
}
