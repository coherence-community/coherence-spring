/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.Optional;

import com.tangosol.net.SessionConfiguration;

/**
 * A provider of {@link com.tangosol.net.SessionConfiguration} instances.
 *
 * @author Jonathan Knight
 * @author Gunnar Hillert
 * @since 3.0
 */
public interface SessionConfigurationProvider {

	/**
	 * Returns the optional {@link SessionConfiguration} that is provider provides.
	 * @return the optional {@link SessionConfiguration} that is provider provides
	 *         or an empty {@link Optional} if this provider cannot provide a
	 *         configuration
	 */
	Optional<SessionConfiguration> getConfiguration();
}
