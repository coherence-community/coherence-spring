/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

import com.oracle.coherence.spring.CoherenceInstance;
import com.oracle.coherence.spring.annotation.EnableCoherence;

/**
 * Activates Coherence Auto Configuration for Spring Boot, provided the respective
 * {@link CoherenceInstance} is not defined.
 *
 * @author Gunnar Hillert
 *
 */
@Configuration
@ConditionalOnMissingBean(CoherenceInstance.class)
@EnableCoherence
public class CoherenceAutoconfiguration {
}
