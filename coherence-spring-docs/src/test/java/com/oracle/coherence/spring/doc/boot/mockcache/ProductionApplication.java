/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.boot.mockcache;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Intentionally discoverable fixture for bootstrap metadata controls. The controls inspect
 * its metadata without starting it. A future bare {@code @SpringBootTest} in this package
 * or a descendant test package may discover this application and load its explicit
 * {@code @EnableCoherence} configuration. Tests using the mock path should explicitly
 * select their isolated bootstrap.
 */
@SpringBootApplication
@EnableCoherence
public class ProductionApplication {

	@Bean
	String productionMarker() {
		return "production";
	}

}
