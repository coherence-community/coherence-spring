/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import java.time.Duration;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration;
import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.session.autoconfigure.SessionProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceSpringSessionAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class))
			.withConfiguration(AutoConfigurations.of(SessionProperties.class))
			.withConfiguration(AutoConfigurations.of(ServerProperties.class))
			.withConfiguration(AutoConfigurations.of(CoherenceSpringSessionAutoConfiguration.class))
			.withInitializer(new ConfigDataApplicationContextInitializer());

	@Test
	void testAutoConfigurationDisabledWithStoreTypeSetToNone() {
		this.contextRunner.withPropertyValues("spring.session.store-type=none")
				.run((context) -> assertThat(context).doesNotHaveBean(SessionRepository.class));
	}

	@Test
	void testAutoConfigurationDisabledWithCoherenceSpringSessionDisabled() {
		this.contextRunner.withPropertyValues("coherence.spring.session.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean(SessionRepository.class));
	}

	@Test
	void testAutoConfigurationDisabledWithCoherenceSpringSessionEnabled() {
		this.contextRunner.withPropertyValues("coherence.spring.session.enabled=true")
				.run((context) -> assertThat(context).hasSingleBean(SessionRepository.class));
	}

	@Test
	void testAutoConfigurationWithUseEntryProcessorTrue() {
		this.contextRunner.withPropertyValues("coherence.spring.session.use-entry-processor=true")
				.run((context) -> {
					assertThat(context).hasSingleBean(SessionRepository.class);
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					assertThat(sessionRepository.isUseEntryProcessor()).isTrue();
				});
	}

	@Test
	void testAutoConfigurationWithUseEntryProcessorTrueButNotSet() {
		this.contextRunner.withPropertyValues("coherence.spring.session.enabled=true")
				.run((context) -> {
					assertThat(context).hasSingleBean(SessionRepository.class);
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					assertThat(sessionRepository.isUseEntryProcessor()).isTrue();
				});
	}

	@Test
	void testAutoConfigurationWithUseEntryProcessorFalse() {
		this.contextRunner.withPropertyValues("coherence.spring.session.use-entry-processor=false")
				.run((context) -> {
					assertThat(context).hasSingleBean(SessionRepository.class);
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					assertThat(sessionRepository.isUseEntryProcessor()).isFalse();
				});
	}

	@Test
	void sessionTimeoutTakesPrecedenceOverServerSessionTimeout() {
		this.contextRunner
				.withPropertyValues("spring.session.timeout=15m", "server.servlet.session.timeout=30m")
				.run((context) -> {
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					final Session session = sessionRepository.createSession();
					assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(15));
				});
	}

	@Test
	void serverSessionTimeoutIsUsedAsFallback() {
		this.contextRunner.withPropertyValues("server.servlet.session.timeout=20m").run((context) -> {
			final CoherenceIndexedSessionRepository sessionRepository =
					context.getBean(CoherenceIndexedSessionRepository.class);
			final Session session = sessionRepository.createSession();
			assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(20));
		});
	}
}
