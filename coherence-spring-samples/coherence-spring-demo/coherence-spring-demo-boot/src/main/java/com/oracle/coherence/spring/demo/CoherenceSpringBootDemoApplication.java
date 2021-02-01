/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Entry point to the Coherence Spring Boot Demo Application.
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication
@EnableCaching
public class CoherenceSpringBootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoherenceSpringBootDemoApplication.class, args);
	}

}
