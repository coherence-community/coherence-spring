/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.coherence.spring.configuration.annotation.CoherenceCache;
import com.oracle.coherence.spring.samples.session.filter.AuthenticationRequest;
import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;
import com.tangosol.net.NamedCache;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Base64Utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Gunnar Hillert
 */
@SpringBootTest(properties = { "spring.session.timeout=5s" })
@AutoConfigureMockMvc
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SessionTimeoutTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@CoherenceCache(CoherenceIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME)
	private NamedCache sessionCache;

	@Test
	public void shouldReturnOk() throws Exception {
		this.mockMvc
				.perform(get("/hello").header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("coherence:rocks".getBytes())))
				.andExpect(status().isOk())
				.andExpect(content().string(is("Hello Coherence")));

		assertThat(this.sessionCache).hasSize(1);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
				assertThat(this.sessionCache).hasSize(0));

		this.mockMvc.perform(get("/hello")).andDo(print()).andExpect(status().isUnauthorized())
				.andExpect(content().string(is(emptyString())));
	}

	@Test
	public void login() throws Exception {
		MockHttpSession session = new MockHttpSession();
		final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.setUsername("coherence");
		authenticationRequest.setPassword("rocks");
		MvcResult result =  this.mockMvc
				.perform(post("/login").content(this.objectMapper.writeValueAsString(authenticationRequest)).session(
						session
				))
				.andExpect(status().isOk())

				.andExpect(content().string(is(emptyString())))
				.andExpect(cookie().exists("JSESSIONID")).andReturn();

		final MockCookie sessionIdCookie = (MockCookie) result.getResponse().getCookie("JSESSIONID");
		MatcherAssert.assertThat(sessionIdCookie.getSameSite(), is("Strict"));

		this.mockMvc.perform(get("/hello").cookie(sessionIdCookie))
				.andExpect(status().isOk())
				.andExpect(content().string(is("Hello Coherence")));

		assertThat(this.sessionCache).hasSize(1);

		await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
				assertThat(this.sessionCache).hasSize(0));

		this.mockMvc.perform(get("/hello")).andDo(print()).andExpect(status().isUnauthorized())
				.andExpect(content().string(is(emptyString())));
	}

}
