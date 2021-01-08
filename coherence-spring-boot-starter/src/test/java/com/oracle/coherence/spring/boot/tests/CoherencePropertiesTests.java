/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.oracle.coherence.spring.boot.autoconfigure.support.LogType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.oracle.coherence.spring.boot.autoconfigure.CoherenceProperties;
import com.tangosol.net.Coherence;
import com.tangosol.net.SessionConfiguration;
import com.tangosol.net.SessionConfiguration.ConfigurableCacheFactorySessionConfig;

@SpringBootTest(classes = CoherencePropertiesTests.class)
@EnableConfigurationProperties(value = CoherenceProperties.class)
@ActiveProfiles({"coherencePropertiesTests"})
public class CoherencePropertiesTests {

	@Autowired
	private CoherenceProperties coherenceProperties;

	@Test
	void testCoherencePropertiesWithSessions() {
		assertEquals(3, coherenceProperties.getSessions().size());
		assertThat(coherenceProperties.getSessions().get(0).getName()).isEqualTo("default");
		assertThat(coherenceProperties.getSessions().get(0).getConfig()).isEqualTo("coherence-cache-config.xml");
		assertThat(coherenceProperties.getSessions().get(0).getScopeName()).isEqualTo("fooscope");
		assertThat(coherenceProperties.getSessions().get(0).getPriority()).isEqualTo(1);
		assertThat(coherenceProperties.getSessions().get(1).getName()).isEqualTo("test");
		assertThat(coherenceProperties.getSessions().get(1).getConfig()).isEqualTo("test-coherence-config.xml");
		assertThat(coherenceProperties.getSessions().get(1).getScopeName()).isEqualTo("barscope");
		assertThat(coherenceProperties.getSessions().get(1).getPriority()).isEqualTo(2);
		assertThat(coherenceProperties.getSessions().get(2).getName()).isNull();
		assertThat(coherenceProperties.getSessions().get(2).getConfig()).isEqualTo("test-coherence-config.xml");
		assertThat(coherenceProperties.getSessions().get(2).getScopeName()).isEqualTo("myscope");
		assertThat(coherenceProperties.getSessions().get(2).getPriority()).isEqualTo(0);
	}

	@Test
	void testCoherenceConfiguration() {
		assertThat(coherenceProperties.getSessions().get(0).getConfiguration()).isNotNull();
		assertThat(coherenceProperties.getSessions().get(1).getConfiguration()).isNotNull();
		assertThat(coherenceProperties.getSessions().get(2).getConfiguration()).isNotNull();

		final SessionConfiguration sessionConfiguration1 = coherenceProperties.getSessions().get(0).getConfiguration().get();
		final SessionConfiguration sessionConfiguration2 = coherenceProperties.getSessions().get(1).getConfiguration().get();
		final SessionConfiguration sessionConfiguration3 = coherenceProperties.getSessions().get(2).getConfiguration().get();

		assertThat(sessionConfiguration1.getName()).isEqualTo(Coherence.DEFAULT_NAME);
		validateConfigUri("coherence-cache-config.xml", sessionConfiguration1);
		assertThat(sessionConfiguration1.getScopeName()).isEqualTo("fooscope");
		assertThat(sessionConfiguration1.getPriority()).isEqualTo(1);
		assertThat(sessionConfiguration2.getName()).isEqualTo("test");
		validateConfigUri("test-coherence-config.xml", sessionConfiguration2);
		assertThat(sessionConfiguration2.getScopeName()).isEqualTo("barscope");
		assertThat(sessionConfiguration2.getPriority()).isEqualTo(2);
		assertThat(sessionConfiguration3.getName()).isEqualTo(Coherence.DEFAULT_NAME);
		validateConfigUri("test-coherence-config.xml", sessionConfiguration3);
		assertThat(sessionConfiguration3.getScopeName()).isEqualTo("myscope");
		assertThat(sessionConfiguration3.getPriority()).isEqualTo(SessionConfiguration.DEFAULT_PRIORITY);
	}

	@Test
	void testCoherenceLoggingProperties() {
		assertNotNull(coherenceProperties.getLogging());
		assertThat(coherenceProperties.getLogging().getCharacterLimit()).isEqualTo(123);
		assertThat(coherenceProperties.getLogging().getDestination()).isEqualTo(LogType.SLF4J);
		assertThat(coherenceProperties.getLogging().getLoggerName()).isEqualTo("testing");
		assertThat(coherenceProperties.getLogging().getMessageFormat()).isEqualTo("Testing: {date}/{uptime} {product} {version} <{level}> (thread={thread}, member={member}): {text}");
	}

	@Test
	void testNativeCoherenceProperties() {
		assertEquals(5, coherenceProperties.getProperties().size());
		assertThat(coherenceProperties.getProperties().get("coherence.log.limit")).isEqualTo("444");
		assertThat(coherenceProperties.getProperties().get("coherence.log.level")).isEqualTo("1");
		assertThat(coherenceProperties.getProperties().get("coherence.log.logger")).isEqualTo("CoherenceSpring");
		assertThat(coherenceProperties.getProperties().get("coherence.log")).isEqualTo("log4j");
		assertThat(coherenceProperties.getProperties().get("coherence.log.format")).isEqualTo("foobar");
	}

	private void validateConfigUri(String expectedConfigUri, SessionConfiguration sessionConfiguration) {
		final ConfigurableCacheFactorySessionConfig configurableCacheFactorySessionConfig =
			(ConfigurableCacheFactorySessionConfig) sessionConfiguration;

		final String actualConfigUri = configurableCacheFactorySessionConfig.getConfigUri().get();

		assertThat(actualConfigUri).isNotBlank();
		assertThat(expectedConfigUri).isEqualTo(actualConfigUri);
	}
}
