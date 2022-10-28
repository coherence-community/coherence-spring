/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import java.util.Map;

import com.oracle.coherence.spring.test.junit.CoherenceServerJunitExtension;
import com.tangosol.net.Coherence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootTest(classes = {
		CoherenceConfigDataLoaderTests.DataLoaderConfig.class,
		CoherenceConfigClientProperties.class
})
@ActiveProfiles("custom")
@DirtiesContext
public class CoherenceConfigDataLoaderTests {

	@RegisterExtension
	static CoherenceServerJunitExtension coherenceServerJunitExtension =
			new CoherenceServerJunitExtension(true);

	@Autowired
	Environment env;

	final Coherence coherence;

	public CoherenceConfigDataLoaderTests(Coherence coherence) {
		this.coherence = coherence;
		final Map<String, Object> properties = this.coherence.getSession().getMap("berlin-kona");
		//properties.put("foo", "remote");
		properties.put("test.foo", "bar");
		properties.put("test.numeric", 1234);
	}

	@Test
	public void testDataLoader() {
			assertThat(this.env.getProperty("foo")).isEqualTo("bar");
			assertThat(this.env.getProperty("test.foo")).isEqualTo("bar");
	}

	@Configuration
	static class DataLoaderConfig {
	}

}
