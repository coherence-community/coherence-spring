/*
 * Copyright (c) 2021, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session;

import com.tangosol.net.Coherence;
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
class CoherenceServerApplicationTests {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private Coherence coherence;

	@Test
	void startsStorageWithoutHttpSessionIntegration() {
		assertThat(this.coherence.isStarted()).isTrue();
		assertThat(this.coherence.getSession().getCache("spring:session:sessions").getCacheService().isRunning())
				.isTrue();
		assertThat(this.context.getBeansOfType(SessionRepository.class)).isEmpty();
		assertThat(this.context.getBeansOfType(SessionRepositoryFilter.class)).isEmpty();
		assertThat(this.context.containsBean("springSessionRepositoryFilter")).isFalse();
		assertThat(this.context.getEnvironment().getProperty("spring.session.store-type")).isEqualTo("none");
	}

}
