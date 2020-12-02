/*
 * File: CoherenceAutoconfiguration.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
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
