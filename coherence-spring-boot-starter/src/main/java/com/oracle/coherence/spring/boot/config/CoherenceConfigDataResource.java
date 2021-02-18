/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.coherence.spring.boot.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;
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

	private final boolean optional;

	private final Profiles profiles;

	public CoherenceConfigDataResource(CoherenceConfigClientProperties properties, boolean optional, Profiles profiles) {
		this.properties = properties;
		this.optional = optional;
		this.profiles = profiles;
	}

	public CoherenceConfigClientProperties getProperties() {
		return this.properties;
	}

	public boolean isOptional() {
		return this.optional;
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
		return this.optional == that.optional && this.properties.equals(that.properties) && this.profiles.equals(that.profiles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.properties, this.optional, this.profiles);
	}

	@Override
	public String toString() {
		return new ToStringCreator(this)
				.append("client", this.properties.getClient())
				.append("optional", this.optional)
				.append("profiles", this.profiles.getAccepted()).toString();

	}
}
