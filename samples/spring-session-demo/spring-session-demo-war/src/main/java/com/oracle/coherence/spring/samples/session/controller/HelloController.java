/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session.controller;

import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A simple Spring MVC Controller that increments a counter on each request.
 * The counter is stored in the {@link HttpSession} which ultimately is persisted to Coherence.
 * @author Gunnar Hillert
 */
@Controller
@RequestMapping
public class HelloController {

	private static final Logger logger = LogManager.getLogger(HelloController.class);

	@Autowired
	private HttpSession session;

	/**
	 * Increments a counter on each request.
	 * @return a simple String containing the counter and the session id.
	 */
	@GetMapping("/hello")
	public String hello() {
		Integer counter = (Integer) this.session.getAttribute("counter");
		if (counter == null) {
			counter = 1;
			this.session.setAttribute("counter", counter);
		}
		else {
			counter++;
			this.session.setAttribute("counter", counter);
		}

		logger.info("Session ID: {}; counter = {}", this.session.getId(), counter);
		return "hello";
	}
}
