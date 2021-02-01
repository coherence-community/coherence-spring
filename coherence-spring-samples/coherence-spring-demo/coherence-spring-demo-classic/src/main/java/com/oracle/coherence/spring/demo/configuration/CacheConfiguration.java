/*
 * Copyright (c) 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.demo.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;

@Configuration
@Profile("!xml")
@EnableCaching
@EnableCoherence
public class CacheConfiguration {

}
