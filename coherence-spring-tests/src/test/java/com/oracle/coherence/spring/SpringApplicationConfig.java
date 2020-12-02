/*
 * File: SpringApplicationConfig.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
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
