package com.oracle.coherence.spring.boot.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.core.io.support.SpringFactoriesLoader;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceAutoConfiguration;

public class CoherenceSpringFactoryTests {

	@Test
	void testPresenceOfAutoConfigurationClass() {
		List<String> auoConfigurationClasses = SpringFactoriesLoader.loadFactoryNames(EnableAutoConfiguration.class, getClass().getClassLoader());
		assertThat(auoConfigurationClasses).contains(CoherenceAutoConfiguration.class.getName());
		assertThat(auoConfigurationClasses).contains(CacheAutoConfiguration.class.getName());
	}
}
