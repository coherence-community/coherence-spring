/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point to the Coherence Cache Store Demo Application.
 *
 * @author Jonathan Knight 2021.08.17
 */
@SpringBootApplication
public class CacheStoreDemo {

	public static void main(String[] args) {
		SpringApplication.run(CacheStoreDemo.class, args);
	}

}
