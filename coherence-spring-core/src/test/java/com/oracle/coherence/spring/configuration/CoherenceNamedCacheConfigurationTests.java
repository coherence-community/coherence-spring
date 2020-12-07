package com.oracle.coherence.spring.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.oracle.coherence.inject.Name;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.tangosol.config.annotation.Injectable;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;

@SpringJUnitConfig(CoherenceNamedCacheConfigurationTests.Config.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class CoherenceNamedCacheConfigurationTests {

	@Configuration
	@EnableCoherence
	static class Config {

	}

//	@Autowired
//	@Name("fooCache")
//	private NamedCache<String, String> fooCache;

//	@Test
//	@Order(1)
//	public void getDefaultSession() throws Exception {
//		Assertions.assertEquals(0, fooCache.size());
//	}

}
