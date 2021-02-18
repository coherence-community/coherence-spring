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
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.util.StringUtils;

/**
 * {@link ConfigDataLocationResolver} for Coherence Spring resolving {@link CoherenceConfigDataResource}
 * using the {@link #COHERENCE_PREFIX} prefix.
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherenceConfigDataLocationResolver implements ConfigDataLocationResolver<CoherenceConfigDataResource> {

	protected static final Log logger = LogFactory.getLog(CoherenceConfigDataLocationResolver.class);

	/**
	 * Prefix used to indicate a {@link CoherenceConfigDataResource}.
	 */
	public static final String COHERENCE_PREFIX = "coherence:";

	/**
	 * Will return true only if the provided {@link ConfigDataLocation} has the prefix as defined by {@link #getPrefix()}
	 * and if the configuration for the Coherence backend is enabled.
	 * @param context the ConfigDataLocationResolverContext
	 * @param location the ConfigDataLocation
	 * @return true if this ConfigDataLocationResolver is applicable to the provided location
	 * @see CoherenceConfigClientProperties
	 */
	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
		if (!location.hasPrefix(getPrefix())) {
			return false;
		}
		return context.getBinder().bind(CoherenceConfigClientProperties.PREFIX + ".enabled", Boolean.class).orElse(true);
	}

	/**
	 * Will return {@link #COHERENCE_PREFIX}.
	 * @return the prefix constant COHERENCE_PREFIX
	 */
	protected String getPrefix() {
		return COHERENCE_PREFIX;
	}

	/**
	 * Not used. Returns an empty list.
	 * @param context not used
	 * @param location not used
	 * @return an empty List
	 */
	@Override
	public List<CoherenceConfigDataResource> resolve(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
		return Collections.emptyList();
	}

	/**
	 * Return a list of CoherenceConfigDataResources.
	 * @param resolverContext the location resolver context
	 * @param location the location that should be resolved
	 * @param profiles profile information
	 * @return a list of resolved CoherenceConfigDataResources (contains 1 element only).
	 * @throws ConfigDataLocationNotFoundException on a non-optional location that cannot
	 * be found
	 */
	@Override
	public List<CoherenceConfigDataResource> resolveProfileSpecific(
			ConfigDataLocationResolverContext resolverContext,
			ConfigDataLocation location, Profiles profiles)
			throws ConfigDataLocationNotFoundException {

		final CoherenceConfigClientProperties properties = loadProperties(resolverContext);

		//TODO Currently not supported and actual uris following the COHERENCE_PREFIX are ignored
		final String uris = location.getNonPrefixedValue(getPrefix());

		if (StringUtils.hasText(uris)) {
			if (logger.isWarnEnabled()) {
				logger.warn("Uri values following the prefix '" + getPrefix() + "' are not currently supported and "
						+ "are ignore. However, you specified: " + uris);
			}
		}

		final ConfigurableBootstrapContext bootstrapContext = resolverContext.getBootstrapContext();
		bootstrapContext.registerIfAbsent(CoherenceConfigClientProperties.class, BootstrapRegistry.InstanceSupplier.of(properties));
		bootstrapContext.addCloseListener((event) -> event.getApplicationContext().getBeanFactory().registerSingleton(
				"coherenceConfigDataConfigClientProperties", event.getBootstrapContext().get(CoherenceConfigClientProperties.class)));

		final List<CoherenceConfigDataResource> locations = new ArrayList<>(1);
		locations.add(new CoherenceConfigDataResource(properties, location.isOptional(), profiles));

		return locations;
	}

	/**
	 * Load the {@link CoherenceConfigClientProperties}. Also, if {@link CoherenceConfigClientProperties#getApplicationName()} is not
	 * set, then default to the value {@code spring.application.name} if available, of if that value is not available,
	 * either, default to the name {@code application}.
	 * @param context the ConfigDataLocationResolverContext
	 * @return returns either the CoherenceConfigClientProperties from the ConfigDataLocationResolverContext or a new
	 * instance of CoherenceConfigClientProperties
	 */
	protected CoherenceConfigClientProperties loadProperties(ConfigDataLocationResolverContext context) {
		final Binder binder = context.getBinder();
		final BindHandler bindHandler = getBindHandler(context);
		final CoherenceConfigClientProperties configClientProperties = binder
				.bind(CoherenceConfigClientProperties.PREFIX, Bindable.of(CoherenceConfigClientProperties.class), bindHandler)
				.orElseGet(CoherenceConfigClientProperties::new);
		if (!StringUtils.hasText(configClientProperties.getApplicationName())) {
			final String applicationName = binder.bind("spring.application.name", Bindable.of(String.class), bindHandler)
					.orElse("application");
			configClientProperties.setApplicationName(applicationName);
		}
		return configClientProperties;
	}

	private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
		return context.getBootstrapContext().getOrElse(BindHandler.class, null);
	}
}
