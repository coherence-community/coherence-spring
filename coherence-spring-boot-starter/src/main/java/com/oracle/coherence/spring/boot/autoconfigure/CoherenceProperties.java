/*
 * File: CoherenceProperties.java
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Configuration properties for the Coherence Spring integration.
 *
 * @author Gunnar Hillert
 */
@ConfigurationProperties(prefix = "spring.coherence")
public class CoherenceProperties {

	/**
	 * The location of the configuration file to use to initialize Coherence.
	 */
	private Resource config;

	public Resource getConfig() {
		return this.config;
	}

	public void setConfig(Resource config) {
		this.config = config;
	}

	/**
	 * Resolve the config location if set.
	 * @return the location or {@code null} if it is not set
	 * @throws IllegalArgumentException if the config attribute is set to an unknown
	 * location
	 */
	public Resource resolveConfigLocation() {
		if (this.config == null) {
			return null;
		}
		Assert.isTrue(this.config.exists(),
				() -> "Coherence configuration does not exist '" + this.config.getDescription() + "'");
		return this.config;
	}

}