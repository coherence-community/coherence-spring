package com.oracle.coherence.spring.boot.tests.event;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

@SpringBootTest(classes = CoherenceEventListenerTest.Config.class)
public class CoherenceEventListenerTest {

	//	@Autowired
	//	Coherence coherence;

	@Test
	public void testCoherenceEventListener() {

	}

	@Configuration
	@EnableCoherence
	static class Config {
	}
}
