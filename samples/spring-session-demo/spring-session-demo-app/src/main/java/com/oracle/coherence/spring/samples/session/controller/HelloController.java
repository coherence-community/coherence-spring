/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session.controller;

import com.tangosol.net.Coherence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main REST Controller. Responds to a GET for the URL {@code /hello} that returns the string {@code Hello World}.
 * @author Gunnar Hillert
 */
@RestController
public class HelloController {

	@Autowired
	private FindByIndexNameSessionRepository findByIndexNameSessionRepository;

	@Autowired
	private Coherence coherence;

	@GetMapping("/hello")
	public String helloWorld() {
		return "Hello Coherence";
	}
}
