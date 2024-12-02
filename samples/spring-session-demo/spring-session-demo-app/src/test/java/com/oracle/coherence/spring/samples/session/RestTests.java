/*
 * Copyright (c) 2021, 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session;

import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oracle.coherence.spring.samples.session.filter.AuthenticationRequest;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Gunnar Hillert
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@Order(1)
	public void shouldReturnDefaultMessage() throws Exception {
		this.mockMvc.perform(get("/hello")).andDo(print()).andExpect(status().isUnauthorized())
				.andExpect(content().string(is(emptyString())));
	}

	@Test
	@Order(2)
	public void shouldReturnOk() throws Exception {
		this.mockMvc
				.perform(get("/hello").header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64.getEncoder().encodeToString("coherence:rocks".getBytes())))
				.andExpect(status().isOk())
				.andExpect(content().string(is("Hello Coherence")));
	}

	@Test
	@Order(3)
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
				.andDo(log())
				.andExpect(content().string(is(emptyString())))
				.andExpect(cookie().exists("JSESSIONID")).andReturn();

		final MockCookie sessionIdCookie = (MockCookie) result.getResponse().getCookie("JSESSIONID");
		MatcherAssert.assertThat(sessionIdCookie.getSameSite(), is("Strict"));

		this.mockMvc.perform(get("/hello").cookie(sessionIdCookie))
				.andExpect(status().isOk())
				.andExpect(content().string(is("Hello Coherence")));
	}

	@Test
	@Order(4)
	public void wrongLogin() throws Exception {
		MockHttpSession session = new MockHttpSession();
		final AuthenticationRequest authenticationRequest = new AuthenticationRequest();
		authenticationRequest.setUsername("wronguser");
		authenticationRequest.setPassword("wrongpassword");
		MvcResult result =  this.mockMvc
				.perform(post("/login").content(this.objectMapper.writeValueAsString(authenticationRequest)).session(
						session
				))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string(is(emptyString())))
				.andExpect(cookie().doesNotExist("JSESSIONID")).andReturn();
	}
}
