/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.StringUtils;

/**
 * Represents a Coherence-specific {@link ConfigDataResource} resource from which ConfigData can be loaded.
 * @author Gunnar Hillert
 * @since 3.0
 * @see CoherenceConfigDataLocationResolver
 * @see CoherenceConfigDataLoader
 */
public class CoherenceConfigDataResource extends ConfigDataResource {

	private final CoherenceConfigClientProperties properties;

	private final Profiles profiles;

	public CoherenceConfigDataResource(CoherenceConfigClientProperties properties, boolean optional, Profiles profiles) {
		super(optional);
		this.properties = properties;
		this.profiles = profiles;
	}

	public CoherenceConfigClientProperties getProperties() {
		return this.properties;
	}

	public String getProfiles() {
		final List<String> accepted = this.profiles.getAccepted();
		if (StringUtils.hasText(this.properties.getProfile())
				&& !this.properties.getProfile().equals(CoherenceConfigClientProperties.DEFAULT_PROFILE)) {
			return this.properties.getProfile();
		}
		return StringUtils.collectionToCommaDelimitedString(accepted);
	}

	public List<String> getProfilesAsList() { //TODO
		final List<String> profilesToReturn = new ArrayList<>();

		if (StringUtils.hasText(this.properties.getProfile())
				&& !this.properties.getProfile().equals(CoherenceConfigClientProperties.DEFAULT_PROFILE)) {
			profilesToReturn.add(this.properties.getProfile());
		}
		else {
			List<String> accepted = this.profiles.getAccepted();
			profilesToReturn.addAll(accepted);
		}
		return profilesToReturn;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CoherenceConfigDataResource that = (CoherenceConfigDataResource) o;
		return this.properties.equals(that.properties) && this.profiles.equals(that.profiles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.properties, this.profiles);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("sessionName", this.properties.getSessionName())
				.append("cacheConfig", this.properties.getCacheConfig())
				.append("profiles", this.profiles.getAccepted()).toString();

	}

	@Configuration
	@EnableCoherence
	static class CoherenceConfig {
		@Bean
		SessionConfigurationBean sessionConfigurationBean() {
			SessionConfigurationBean sessionConfigurationBean = new SessionConfigurationBean();
			sessionConfigurationBean.setName("");

			return sessionConfigurationBean;
		}
	}
}
