/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry to the application.
 * @author Gunnar Hillert
 */
@SpringBootApplication
public class CoherenceServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoherenceServerApplication.class, args);
	}

}
