/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration;
import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;
import com.tangosol.net.Coherence;
import jakarta.servlet.DispatcherType;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.session.autoconfigure.SessionAutoConfiguration;
import org.springframework.boot.session.autoconfigure.SessionTimeout;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.context.servlet.AnnotationConfigServletWebApplicationContext;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.servlet.DelegatingFilterProxyRegistrationBean;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SaveMode;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceSpringSessionAutoConfigurationTests {

	// An existing ServletContext selects Boot's WAR deployment configuration.
	private final WebApplicationContextRunner warContextRunner = sessionContextRunner(new WebApplicationContextRunner());

	// Embedded applications have no ServletContext when auto-configuration conditions run.
	private final WebApplicationContextRunner embeddedContextRunner = sessionContextRunner(
			new WebApplicationContextRunner(AnnotationConfigServletWebApplicationContext::new));

	private static WebApplicationContextRunner sessionContextRunner(WebApplicationContextRunner runner) {
		return runner.withConfiguration(AutoConfigurations.of(CoherenceAutoConfiguration.class,
				CoherenceSpringSessionAutoConfiguration.class, SessionAutoConfiguration.class))
				.withInitializer(new ConfigDataApplicationContextInitializer());
	}

	@Test
	void testAutoConfigurationDisabledWithStoreTypeSetToNone() {
		this.warContextRunner.withPropertyValues("spring.session.store-type=none")
				.run((context) -> assertThat(context).doesNotHaveBean(SessionRepository.class));
	}

	@Test
	void testAutoConfigurationDisabledWithCoherenceSpringSessionDisabled() {
		this.warContextRunner.withPropertyValues("coherence.spring.session.enabled=false")
				.run((context) -> assertThat(context).doesNotHaveBean(SessionRepository.class));
	}

	@Test
	void testAutoConfigurationEnabledWithCoherenceSpringSessionEnabled() {
		this.warContextRunner.withPropertyValues("coherence.spring.session.enabled=true")
				.run((context) -> {
					assertThat(context).hasSingleBean(SessionRepository.class);
					assertThat(context).hasSingleBean(SessionRepositoryFilter.class);
					assertThat(context).hasBean("sessionRepositoryFilterRegistration");
				});
	}

	@Test
	void testAutoConfigurationBacksOffWithAlternativeStoreType() {
		this.warContextRunner.withPropertyValues("spring.session.store-type=redis")
				.run((context) -> assertThat(context).doesNotHaveBean(CoherenceIndexedSessionRepository.class));
	}

	@Test
	void testAutoConfigurationBacksOffWithCustomSessionRepository() {
		MapSessionRepository repository = new MapSessionRepository(new ConcurrentHashMap<>());
		this.warContextRunner.withBean(SessionRepository.class, () -> repository).run((context) -> {
			assertThat(context).hasSingleBean(SessionRepository.class);
			assertThat(context).doesNotHaveBean(CoherenceIndexedSessionRepository.class);
			assertThat(context.getBean(SessionRepository.class)).isSameAs(repository);
		});
	}

	@Test
	void testAutoConfigurationWithUseEntryProcessorTrue() {
		this.warContextRunner.withPropertyValues("coherence.spring.session.use-entry-processor=true")
				.run((context) -> {
					assertThat(context).hasSingleBean(SessionRepository.class);
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					assertThat(sessionRepository.isUseEntryProcessor()).isTrue();
				});
	}

	@Test
	void testAutoConfigurationWithUseEntryProcessorTrueButNotSet() {
		this.warContextRunner.withPropertyValues("coherence.spring.session.enabled=true")
				.run((context) -> {
					assertThat(context).hasSingleBean(SessionRepository.class);
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					assertThat(sessionRepository.isUseEntryProcessor()).isTrue();
				});
	}

	@Test
	void testAutoConfigurationWithUseEntryProcessorFalse() {
		this.warContextRunner.withPropertyValues("coherence.spring.session.use-entry-processor=false")
				.run((context) -> {
					assertThat(context).hasSingleBean(SessionRepository.class);
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					assertThat(sessionRepository.isUseEntryProcessor()).isFalse();
				});
	}

	@Test
	void sessionTimeoutTakesPrecedenceOverServerSessionTimeout() {
		this.embeddedContextRunner
				.withPropertyValues("spring.session.timeout=15m", "server.servlet.session.timeout=30m")
				.run((context) -> {
					assertThat(context).hasSingleBean(ServerProperties.class);
					assertThat(context).hasBean("embeddedWebServerSessionTimeout");
					final CoherenceIndexedSessionRepository sessionRepository =
							context.getBean(CoherenceIndexedSessionRepository.class);
					final Session session = sessionRepository.createSession();
					assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(15));
				});
	}

	@Test
	void serverSessionTimeoutIsUsedAsFallback() {
		this.embeddedContextRunner.withPropertyValues("server.servlet.session.timeout=20m").run((context) -> {
			assertThat(context).hasSingleBean(ServerProperties.class);
			assertThat(context).hasBean("embeddedWebServerSessionTimeout");
			final CoherenceIndexedSessionRepository sessionRepository =
					context.getBean(CoherenceIndexedSessionRepository.class);
			final Session session = sessionRepository.createSession();
			assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(20));
		});
	}

	@Test
	void warSessionTimeoutIsAppliedWithoutServerProperties() {
		this.warContextRunner.withPropertyValues("spring.session.timeout=15m").run((context) -> {
			assertThat(context).doesNotHaveBean(ServerProperties.class);
			assertThat(context.getBean(SessionTimeout.class).getTimeout()).isEqualTo(Duration.ofMinutes(15));
			Session session = context.getBean(CoherenceIndexedSessionRepository.class).createSession();
			assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMinutes(15));
		});
	}

	@Test
	void warWithoutSessionTimeoutRetainsRepositoryDefault() {
		this.warContextRunner.run(CoherenceSpringSessionAutoConfigurationTests::assertDefaultWarTimeout);
	}

	@Test
	void warDoesNotUseServerSessionTimeoutAsFallback() {
		this.warContextRunner.withPropertyValues("server.servlet.session.timeout=20m")
				.run(CoherenceSpringSessionAutoConfigurationTests::assertDefaultWarTimeout);
	}

	private static void assertDefaultWarTimeout(AssertableWebApplicationContext context) {
		assertThat(context).doesNotHaveBean(ServerProperties.class);
		assertThat(context.getBean(SessionTimeout.class).getTimeout()).isNull();
		Session session = context.getBean(CoherenceIndexedSessionRepository.class).createSession();
		assertThat(session.getMaxInactiveInterval())
				.isEqualTo(Duration.ofSeconds(MapSession.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS));
	}

	@Test
	void embeddedSessionFilterRegistrationHonorsConfiguredSettings() {
		this.embeddedContextRunner.withPropertyValues("spring.session.servlet.filter-order=123",
				"spring.session.servlet.filter-dispatcher-types=REQUEST,ERROR")
				.run(CoherenceSpringSessionAutoConfigurationTests::assertSessionFilterRegistration);
	}

	@Test
	void warSessionFilterRegistrationHonorsConfiguredSettings() {
		this.warContextRunner.withPropertyValues("spring.session.servlet.filter-order=123",
				"spring.session.servlet.filter-dispatcher-types=REQUEST,ERROR")
				.run(CoherenceSpringSessionAutoConfigurationTests::assertSessionFilterRegistration);
	}

	private static void assertSessionFilterRegistration(AssertableWebApplicationContext context) {
		assertThat(context).hasSingleBean(SessionRepositoryFilter.class);
		assertThat(context).hasBean("sessionRepositoryFilterRegistration");
		DelegatingFilterProxyRegistrationBean registration = context.getBean("sessionRepositoryFilterRegistration",
				DelegatingFilterProxyRegistrationBean.class);
		assertThat(registration.getFilterName()).isEqualTo("springSessionRepositoryFilter");
		assertThat(registration.getOrder()).isEqualTo(123);
		assertThat(registration.determineDispatcherTypes())
				.containsExactlyInAnyOrder(DispatcherType.REQUEST, DispatcherType.ERROR);
	}

	@Test
	void sessionRepositoryHonorsCoherenceProperties() {
		this.warContextRunner.withPropertyValues("coherence.spring.session.map-name=custom-sessions",
				"coherence.spring.session.flush-mode=IMMEDIATE", "coherence.spring.session.save-mode=ALWAYS")
				.run((context) -> {
					CoherenceIndexedSessionRepository repository = context.getBean(CoherenceIndexedSessionRepository.class);
					assertThat(repository.getFlushMode()).isEqualTo(FlushMode.IMMEDIATE);
					assertThat(repository.getSaveMode()).isEqualTo(SaveMode.ALWAYS);
					Session session = repository.createSession();
					assertThat(context.getBean(Coherence.class).getSession().getCache("custom-sessions"))
							.containsKey(session.getId());
				});
	}
}
