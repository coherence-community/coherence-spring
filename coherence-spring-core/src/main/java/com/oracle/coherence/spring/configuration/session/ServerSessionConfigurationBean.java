/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.session;

/**
 * Instances of {@link com.tangosol.net.SessionConfiguration} are used Coherence cluster member configurations.
 *
 * @author Gunnar Hillert
 */
public class ServerSessionConfigurationBean extends SessionConfigurationBean {
	public ServerSessionConfigurationBean() {
		super.setType(SessionType.SERVER);
	}
}
