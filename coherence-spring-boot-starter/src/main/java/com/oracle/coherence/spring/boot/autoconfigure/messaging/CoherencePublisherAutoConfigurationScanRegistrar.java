/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 */
package com.oracle.coherence.spring.boot.autoconfigure.messaging;

import java.util.Collection;
import java.util.Collections;

import com.oracle.coherence.spring.annotation.CoherencePublisherScan;
import com.oracle.coherence.spring.messaging.CoherencePublisherScanRegistrar;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Variation of {@link CoherencePublisherScanRegistrar} the links
 * {@link AutoConfigurationPackages}.
 *
 * @author Artem Bilan
 * @author Phillip Webb
 * @since 3.0
 */
public class CoherencePublisherAutoConfigurationScanRegistrar extends CoherencePublisherScanRegistrar implements BeanFactoryAware {

	private BeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		super.registerBeanDefinitions(AnnotationMetadata.introspect(CoherencePublisherScanConfiguration.class), registry);
	}

	@Override
	protected Collection<String> getBasePackages(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		return AutoConfigurationPackages.has(this.beanFactory)
				? AutoConfigurationPackages.get(this.beanFactory)
				: Collections.emptyList();
	}

	@CoherencePublisherScan
	private static final class CoherencePublisherScanConfiguration {

	}
}
