/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.oracle.coherence.spring.CoherenceServer;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;

/**
 * Activates Coherence Auto Configuration for Spring Boot, provided the respective
 * {@link CoherenceServer} is not defined.
 *
 * @author Gunnar Hillert
 *
 */
@Configuration
@ConditionalOnMissingBean(CoherenceServer.class)
@EnableCoherence
@EnableConfigurationProperties(CoherenceProperties.class)
public class CoherenceAutoconfiguration {
}
