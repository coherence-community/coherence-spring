/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.session.config;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * Contains Spring Session Configuration. The main purpose of using Spring Session is the ability to set
 * the {@code SameSite} cookie attribute to protect against CSRF Attacks.
 * @author Gunnar Hillert
 */
@Configuration
//@EnableCoherence
public class SpringSessionConfig {

	/**
	 * Sets the {@code SameSite} cookie attribute to protect against CSRF Attacks.
	 * @return cookieSerializer
	 */
	@Bean
	public CookieSerializer cookieSerializer() {
		final DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("JSESSIONID");
		serializer.setCookiePath("/");
		serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
		serializer.setSameSite("Strict");
		return serializer;
	}
}
