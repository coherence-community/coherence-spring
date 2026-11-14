/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package consumer;
import org.springframework.context.annotation.Configuration;
import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.session.config.annotation.web.http.EnableCoherenceHttpSession;
@Configuration(proxyBeanMethods = false)
@EnableCoherence
@EnableCoherenceHttpSession(sessionTimeoutInSeconds = 123)
public class Application { }
