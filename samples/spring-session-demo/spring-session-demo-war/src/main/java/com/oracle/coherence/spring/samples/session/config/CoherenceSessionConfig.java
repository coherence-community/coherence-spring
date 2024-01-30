/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session.config;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.configuration.session.SessionConfigurationBean;
import com.oracle.coherence.spring.configuration.session.SessionType;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.FlushMode;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Spring Configuration class that enables Coherence Spring Session. It also defines the {@link SessionConfigurationBean}
 * that is used to configure Coherence Spring as a client using the {@code remote-cache-config.xml} configuration file.
 * @author Gunnar Hillert
 */
@Configuration
@EnableCoherence
@EnableCoherenceHttpSession(
		cache = "spring:session:sessions",
		flushMode = FlushMode.ON_SAVE,
		sessionTimeoutInSeconds = 1800,
		useEntryProcessor = false
)
public class CoherenceSessionConfig extends AbstractHttpSessionApplicationInitializer {

	/**
	 * Configure Coherence Spring as a client using the {@code remote-cache-config.xml} configuration file.
	 * @return the SessionConfigurationBean
	 */
	@Bean
	public SessionConfigurationBean sessionConfigurationBeanDefault() {
		final SessionConfigurationBean sessionConfigurationBean =
				new SessionConfigurationBean();
		sessionConfigurationBean.setType(SessionType.CLIENT);
		sessionConfigurationBean.setConfig("remote-cache-config.xml");
		return sessionConfigurationBean;
	}

}
