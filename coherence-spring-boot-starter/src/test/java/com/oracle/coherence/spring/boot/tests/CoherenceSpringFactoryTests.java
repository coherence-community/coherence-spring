/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.data.CoherenceRepositoriesAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.metrics.CoherenceMetricsAutoConfiguration;
import com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration;
import com.oracle.coherence.spring.boot.config.CoherenceConfigDataLoader;
import com.oracle.coherence.spring.boot.config.CoherenceConfigDataLocationResolver;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.annotation.ImportCandidates;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Gunnar Hillert
 */
public class CoherenceSpringFactoryTests {

	@Test
	void testPresenceOfCoherenceAutoConfigurationClasses() {
		final ImportCandidates importCandidates = ImportCandidates.load(AutoConfiguration.class, this.getClass().getClassLoader());

		final Predicate<String> p1 = (importCandidate) -> importCandidate.equals(CoherenceAutoConfiguration.class.getName());
		final Predicate<String> p2 = (importCandidate) -> importCandidate.equals(CoherenceSpringSessionAutoConfiguration.class.getName());
		final Predicate<String> p3 = (importCandidate) -> importCandidate.equals(CoherenceMetricsAutoConfiguration.class.getName());
		final Predicate<String> p4 = (importCandidate) -> importCandidate.equals(CoherenceRepositoriesAutoConfiguration.class.getName());

		final List<String> coherenceImportCandidates = StreamSupport.stream(importCandidates.spliterator(), false)
				.filter(p1.or(p2).or(p3).or(p4))
				.limit(4).collect(Collectors.toList());
		assertThat(coherenceImportCandidates.size()).isEqualTo(4);
		assertThat(coherenceImportCandidates).containsExactly(
				"com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration",
				"com.oracle.coherence.spring.boot.autoconfigure.session.CoherenceSpringSessionAutoConfiguration",
				"com.oracle.coherence.spring.boot.autoconfigure.metrics.CoherenceMetricsAutoConfiguration",
				"com.oracle.coherence.spring.boot.autoconfigure.data.CoherenceRepositoriesAutoConfiguration"
		);
	}

	@Test
	void testPresenceOfConfigDataClasses() {
		final List<String> configDataResolverClasses = SpringFactoriesLoader.loadFactoryNames(ConfigDataLocationResolver.class, getClass().getClassLoader());
		final List<String> configDataLoaderClasses = SpringFactoriesLoader.loadFactoryNames(ConfigDataLoader.class, getClass().getClassLoader());

		assertThat(configDataResolverClasses).contains(CoherenceConfigDataLocationResolver.class.getName());
		assertThat(configDataLoaderClasses).contains(CoherenceConfigDataLoader.class.getName());
	}
}
