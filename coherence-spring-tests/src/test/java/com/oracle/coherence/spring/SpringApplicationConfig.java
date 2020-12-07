/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;

import com.tangosol.net.NamedCache;

import java.util.HashMap;

/**
 * The Spring-based Test Application Configuration.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@Configuration
public class SpringApplicationConfig
{
	@Bean
	public SpringBasedCoherenceSession session()
	{
		return new SpringBasedCoherenceSession("spring-application-cache-config.xml");
	}

	@Bean
	@Lazy
	public StubNamedCacheStore mapCacheStore()
	{
		HashMap map = new HashMap();

		map.put("key", "value");

		return new StubNamedCacheStore(map);
	}


	@Bean
	@Lazy
	public StubBackingMapListener bml()
	{
		return new StubBackingMapListener();
	}


	@Bean
	@Lazy
	public StubBackingMapListener bmlPull()
	{
		return new StubBackingMapListener();
	}


	@Bean
	@Lazy
	public StubNamedCacheStore mapCacheStorePull()
	{
		HashMap map = new HashMap();

		map.put("key", "value");

		return new StubNamedCacheStore(map);
	}


	@Bean
	@Lazy
	public StubInterceptor interceptor()
	{
		return new StubInterceptor();
	}
}
