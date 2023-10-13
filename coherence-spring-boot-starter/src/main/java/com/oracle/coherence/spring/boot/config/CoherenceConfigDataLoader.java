/*
 * Copyright 2017-2023 original authors
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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.tangosol.net.Session;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * Coherence-specific implementation of Spring Boot's {@link ConfigDataLoader}.
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceConfigDataLoader implements ConfigDataLoader<CoherenceConfigDataResource> {

	@Override
	public ConfigData load(ConfigDataLoaderContext context, CoherenceConfigDataResource resource) {
		return new ConfigData(getPropertySources(resource));
	}

	public List<PropertySource<?>> getPropertySources(CoherenceConfigDataResource resource) {
		try (CoherenceGrpcClient coherenceGrpcClient = new CoherenceGrpcClient(resource.getProperties())) {
			final List<String> keys = this.buildSourceNames(resource);
			final Session session = coherenceGrpcClient.getCoherenceSession();
			return keys.stream()
					.map((propertySourceName) ->
								new MapPropertySource(
										propertySourceName,
										new HashMap<>(session.getMap(propertySourceName))))
					.collect(Collectors.toList());
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Build a list of config source names. These are the key under which the properties are stored by Coherence.
	 * @param coherenceConfigDataResource must not be null
	 * @return a list of config source names
	 */
	protected List<String> buildSourceNames(CoherenceConfigDataResource coherenceConfigDataResource) {
		Assert.notNull(coherenceConfigDataResource, "coherenceConfigDataResource must not be null.");

		final String configuredApplicationName = coherenceConfigDataResource.getProperties().getApplicationName();
		final List<String> profiles = coherenceConfigDataResource.getProfilesAsList();

		return profiles.stream()
				.map((profileName) -> configuredApplicationName + '-' + profileName)
				.collect(Collectors.toList());
	}
}
