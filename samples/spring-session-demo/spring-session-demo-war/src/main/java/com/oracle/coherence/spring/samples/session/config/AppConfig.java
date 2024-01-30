/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration class.
 * @author Gunnar Hillert
 */
@Configuration
@ComponentScan(basePackages = "com.oracle.coherence.spring.samples.session")
public class AppConfig {
}
