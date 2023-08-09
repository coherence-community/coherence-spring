/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.config.annotation.web.http;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.SaveMode;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.SessionRepositoryFilter;

/**
 * Exposes the {@link SessionRepositoryFilter} as a bean named {@code springSessionRepositoryFilter}.
 * Use together with {link {@link com.oracle.coherence.spring.configuration.annotation.EnableCoherence}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 * @see EnableSpringHttpSession
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(CoherenceHttpSessionConfiguration.class)
@Configuration(proxyBeanMethods = false)
public @interface EnableCoherenceHttpSession {

	/**
	 * The session timeout in seconds. By default, it is set to 1800 seconds (30 minutes).
	 * This should be a non-negative integer.
	 * @return the seconds a session can be inactive before expiring
	 */
	int sessionTimeoutInSeconds() default MapSession.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS;

	/**
	 * The name of the cache that will hold the session data. Default is
	 * {@link CoherenceIndexedSessionRepository#DEFAULT_SESSION_MAP_NAME}.
	 * @return the name of the Coherence cache
	 */
	String cache() default CoherenceIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME;

	/**
	 * Name of the Coherence session. If not set will use the default Coherence session.
	 * @return name of the Coherence session
	 */
	String session() default "";

	/**
	 * Flush mode for Coherence sessions. The default is {@code ON_SAVE} which updates the Coherence backend only
	 * when {@link SessionRepository#save(Session)} is invoked. In a web environment this happens just before the
	 * HTTP response is committed.
	 * <p>
	 * Setting the value to {@code IMMEDIATE} will ensure that the any updates to the
	 * Session are immediately written to Coherence.
	 * @return the {@link FlushMode} to use
	 */
	FlushMode flushMode() default FlushMode.ON_SAVE;

	/**
	 * Save mode for the session. The default is {@link SaveMode#ON_SET_ATTRIBUTE}, which only saves changes made to the
	 * session.
	 * @return the save mode
	 */
	SaveMode saveMode() default SaveMode.ON_SET_ATTRIBUTE;

	/**
	 * Specify whether an entry processor shall be used when updating the HTTP session. The default is {@code true}.
	 * @return true if an entry processor is to be used
	 */
	boolean useEntryProcessor() default true;
}
