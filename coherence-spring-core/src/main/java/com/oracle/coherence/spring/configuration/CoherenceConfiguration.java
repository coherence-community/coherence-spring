/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.oracle.coherence.spring.CoherenceInstance;
import com.oracle.coherence.spring.SpringBasedCoherenceSession;

/**
 *
 * @author Gunnar Hillert
 *
 */
@Configuration
public class CoherenceConfiguration {

	@Bean
	CoherenceInstance coherenceInstance(SpringBasedCoherenceSession springBasedCoherenceSession) {
		return new CoherenceInstance(springBasedCoherenceSession);
	}

	@Bean
	SpringBasedCoherenceSession springBasedCoherenceSession() {
		return new SpringBasedCoherenceSession();
	}
}
