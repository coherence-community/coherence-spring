/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.data;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.data.CoherenceRepositoriesAutoConfiguration;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class SpringDataAutoConfigurationTests {

	ConditionEvaluationReportLoggingListener initializer = ConditionEvaluationReportLoggingListener.forLogLevel(
			LogLevel.INFO);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					CoherenceAutoConfiguration.class, CoherenceRepositoriesAutoConfiguration.class))
			.withInitializer(new ConfigDataApplicationContextInitializer())
			.withInitializer(this.initializer);

	@Test
	public void testInitializationOfSpringDataRepository() {
		this.contextRunner.withUserConfiguration(ConfigWithEnableCoherenceRepositoriesAnnotation.class)
		.run((context) -> {
			final Coherence coherence = context.getBean(Coherence.class);
			final Session session = coherence.getSession();
			assertThat(context).hasSingleBean(CoherenceServer.class);
			assertThat(context).hasSingleBean(SpringDataTaskRepository.class);

			final NamedMap<String, Task> tasks = session.getMap("tasks");
			assertThat(tasks).hasSize(0);

			final SpringDataTaskRepository repository = context.getBean(SpringDataTaskRepository.class);
			final Task task = new Task("Write some code.");
			repository.save(task);
			assertThat(tasks).hasSize(1);
			repository.deleteAll();
			assertThat(tasks).hasSize(0);
		});
	}

	@Test
	public void testSpringDataRepositoryAutoConfiguration() {
		this.contextRunner.withUserConfiguration(ConfigWithAutoConfigurationOfCoherenceRepositories.class)
				.run((context) -> {
					final Coherence coherence = context.getBean(Coherence.class);
					final Session session = coherence.getSession();
					assertThat(context).hasSingleBean(CoherenceServer.class);
					assertThat(context).hasSingleBean(SpringDataTaskRepository.class);

					final NamedMap<String, Task> tasks = session.getMap("tasks");
					assertThat(tasks).hasSize(0);

					final SpringDataTaskRepository repository = context.getBean(SpringDataTaskRepository.class);
					final Task task = new Task("Write some code.");
					repository.save(task);
					assertThat(tasks).hasSize(1);
					repository.deleteAll();
					assertThat(tasks).hasSize(0);
				});
	}

	@Test
	void autoConfigurationShouldBackOff() {
		this.contextRunner.withUserConfiguration(BackOffAutoConfiguration.class)
				.run((context) -> assertThat(context).doesNotHaveBean(SpringDataTaskRepository.class));
	}

	@Test
	void autoConfigurationShouldBackOffWithRepositoriesDisabledUsingProperty() {
		this.contextRunner.withUserConfiguration(ConfigWithAutoConfigurationOfCoherenceRepositories.class)
				.withPropertyValues("coherence.spring.data.repositories.enabled", "false")
				.run((context) -> assertThat(context).doesNotHaveBean(SpringDataTaskRepository.class));
	}

	@Configuration
	@EnableCoherenceRepositories
	@AutoConfigurationPackage
	static class ConfigWithEnableCoherenceRepositoriesAnnotation {
	}

	@Configuration
	@AutoConfigurationPackage
	static class ConfigWithAutoConfigurationOfCoherenceRepositories {
	}

	@Configuration(proxyBeanMethods = false)
	//Shall not find any repositories
	@EnableCoherenceRepositories("foo.bar")
	@AutoConfigurationPackage
	static class BackOffAutoConfiguration {

	}
}
