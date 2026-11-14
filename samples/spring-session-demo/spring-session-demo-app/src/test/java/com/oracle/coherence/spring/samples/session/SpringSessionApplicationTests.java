/*
 * Copyright (c) 2021, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session;

import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gunnar Hillert
 */
@SpringBootTest
@DirtiesContext
class SpringSessionApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Test
	void starterEnablesCoherenceHttpSessions() {
		assertThat(this.context.getBeansOfType(SessionRepository.class)).hasSize(1);
		assertThat(this.context.getBean(SessionRepository.class)).isInstanceOf(CoherenceIndexedSessionRepository.class);
		assertThat(this.context.getBeansOfType(SessionRepositoryFilter.class)).hasSize(1);
		assertThat(this.context.getBean("springSessionRepositoryFilter")).isInstanceOf(SessionRepositoryFilter.class);
	}

}
