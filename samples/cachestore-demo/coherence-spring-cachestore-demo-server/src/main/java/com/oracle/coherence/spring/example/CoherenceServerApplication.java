/*
 * Copyright (c) 2022, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example;

import com.oracle.coherence.spring.example.model.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 *
 * @author Gunnar Hillert
 *
 */
@SpringBootApplication
@EnableConfigurationProperties
public class CoherenceServerApplication implements ApplicationRunner {

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private DataSourceProperties ds;

	public static void main(String[] args) {
		SpringApplication.run(CoherenceServerApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println(personRepository.findAll().size());
	}

//	@Bean(destroyMethod = "close")
//	public Coherence coherenceServer() {
//		final SessionConfiguration sessionConfiguration = SessionConfiguration.builder()
//				.withConfigUri("coherence-cache-config.xml")
//				.build();
//
//		final CoherenceConfiguration cfg = CoherenceConfiguration.builder()
//				.withSessions(sessionConfiguration)
//				.build();
//		final Coherence coherence = Coherence.clusterMember(cfg);
//		coherence.start().join();
//
//		return coherence;
//	}
}
