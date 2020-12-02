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