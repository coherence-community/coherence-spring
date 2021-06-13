/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;

/**
 * Customized {@link UsernamePasswordAuthenticationFilter} that reads the {@link AuthenticationRequest} from the request
 * and populates the username and password.
 * @author Gunnar Hillert
 */
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private static final String BODY_ATTRIBUTE = JsonUsernamePasswordAuthenticationFilter.class.getSimpleName() + ".body";

	private final ObjectMapper objectMapper;

	/**
	 * Constructor that initializes the {@link ObjectMapper}.
	 * @param objectMapper must not be null
	 */
	public JsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
		Assert.notNull(objectMapper, "objectMapper must not be null.");
		this.objectMapper = objectMapper;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		final HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		if (requiresAuthentication(httpServletRequest, httpServletResponse)) {
			final AuthenticationRequest authenticationRequest = this.objectMapper.readValue(request.getInputStream(), AuthenticationRequest.class);
			request.setAttribute(BODY_ATTRIBUTE, authenticationRequest);
		}
		super.doFilter(request, response, chain);
	}

	@Override
	protected String obtainUsername(HttpServletRequest request) {
		final AuthenticationRequest authenticationRequest = (AuthenticationRequest) request.getAttribute(BODY_ATTRIBUTE);
		return authenticationRequest.getUsername();
	}

	@Override
	protected String obtainPassword(HttpServletRequest request) {
		final AuthenticationRequest authenticationRequest = (AuthenticationRequest) request.getAttribute(BODY_ATTRIBUTE);
		return authenticationRequest.getPassword();
	}
}
