/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The optional servlet session feature must back off before loading its properties,
 * superclass or Boot timeout signatures.
 */
class CoherenceSpringSessionOptionalDependenciesTests {

	private final AutoConfigurations configurations =
			AutoConfigurations.of(CoherenceSpringSessionAutoConfiguration.class);

	@Test
	void backsOffWithoutBootSession() {
		assertMissingDependency("org.springframework.boot.session");
	}

	@Test
	void backsOffWithoutCoherenceSession() {
		assertMissingDependency("com.oracle.coherence.spring.session");
	}

	@Test
	void backsOffWithoutSpringSession() {
		assertMissingDependency("org.springframework.session");
	}

	@Test
	void backsOffWithoutServletApi() {
		new ApplicationContextRunner().withConfiguration(this.configurations)
				.withClassLoader(new FilteredClassLoader("jakarta.servlet"))
				.withPropertyValues("coherence.spring.session.enabled=true")
				.run((context) -> {
					assertThat(context).hasNotFailed();
					assertBackedOff(context);
					assertThat(ConditionEvaluationReport.get(context.getBeanFactory())
							.getConditionAndOutcomesBySource()
							.get(CoherenceSpringSessionAutoConfiguration.class.getName()))
							.anySatisfy((outcome) -> assertThat(outcome.getOutcome().getMessage())
									.contains("jakarta.servlet"));
				});
	}

	@Test
	void backsOffWithoutBootSessionTimeout() {
		new WebApplicationContextRunner().withConfiguration(this.configurations)
				.withClassLoader(new FilteredClassLoader(
						(name) -> name.equals("org.springframework.boot.session.autoconfigure.SessionTimeout")))
				.run((context) -> {
					assertThat(context).hasNotFailed();
					assertBackedOff(context);
				});
	}

	private void assertMissingDependency(String packageName) {
		for (String property : new String[] {"unrelated.property=true", "coherence.spring.session.enabled=true"}) {
			new WebApplicationContextRunner().withConfiguration(this.configurations)
					.withClassLoader(new FilteredClassLoader(packageName))
					.withPropertyValues(property)
					.run((context) -> {
						assertThat(context).hasNotFailed();
						assertBackedOff(context);
					});
		}
	}

	@Test
	void backsOffInNonWebApplicationWithServletClassesPresent() {
		new ApplicationContextRunner().withConfiguration(this.configurations)
				.run(CoherenceSpringSessionOptionalDependenciesTests::assertBackedOff);
	}

	@Test
	void backsOffInReactiveApplicationWithServletClassesPresent() {
		new ReactiveWebApplicationContextRunner().withConfiguration(this.configurations)
				.run(CoherenceSpringSessionOptionalDependenciesTests::assertBackedOff);
	}

	private static void assertBackedOff(org.springframework.context.ApplicationContext context) {
		assertThat(context.containsBean("sessionRepository")).isFalse();
		assertThat(context.containsBean("springSessionRepositoryFilter")).isFalse();
		assertThat(context.getBeanNamesForType(CoherenceSpringSessionAutoConfiguration.class)).isEmpty();
		assertThat(context.containsBean("coherence.spring.session-com.oracle.coherence.spring.boot.autoconfigure.session."
				+ "CoherenceSpringSessionProperties")).isFalse();
	}
}
