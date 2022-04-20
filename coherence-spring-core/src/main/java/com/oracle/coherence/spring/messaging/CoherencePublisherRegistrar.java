/*
 * Copyright 2014-2019 the original author or authors.
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
package com.oracle.coherence.spring.messaging;

import java.util.Map;

import com.oracle.coherence.spring.annotation.CoherencePublisher;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * The {@link ImportBeanDefinitionRegistrar} to parse {@link CoherencePublisher} and its {@code service-interface}
 * and to register {@link BeanDefinition} {@link CoherencePublisherProxyFactoryBean}.
 *
 * @author Artem Bilan
 * @author Gary Russell
 * @author Andy Wilksinson
 *
 * @since 3.0
 */
public class CoherencePublisherRegistrar implements ImportBeanDefinitionRegistrar {

	private static final String PROXY_DEFAULT_METHODS_ATTR = "proxyDefaultMethods";
	private static final String SERVICE_INTERFACE_ATTR = "serviceInterface";

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (importingClassMetadata != null && importingClassMetadata.isAnnotated(CoherencePublisher.class.getName())) {
			Assert.isTrue(importingClassMetadata.isInterface(),
					"@CoherencePublisher can only be specified on an interface");

			final Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(CoherencePublisher.class.getName());

			if (annotationAttributes == null) {
				throw new IllegalStateException("Unable to retrieve annotationAttributes for CoherencePublisher annotations");
			}

			annotationAttributes.put(SERVICE_INTERFACE_ATTR, importingClassMetadata.getClassName());
			annotationAttributes.put(PROXY_DEFAULT_METHODS_ATTR, "" + annotationAttributes.remove(PROXY_DEFAULT_METHODS_ATTR));
			final BeanDefinitionHolder benDefinitionHolder = parse(annotationAttributes);
			BeanDefinitionReaderUtils.registerBeanDefinition(benDefinitionHolder, registry);
		}
	}

	protected BeanDefinitionHolder parse(Map<String, Object> attributes) {
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(CoherencePublisherProxyFactoryBean.class);

		String proxyDefaultMethods = (String) attributes.get(PROXY_DEFAULT_METHODS_ATTR);
		if (StringUtils.hasText(proxyDefaultMethods)) {
			beanDefinitionBuilder.addPropertyValue(PROXY_DEFAULT_METHODS_ATTR, proxyDefaultMethods);
		}

		String serviceInterface = (String) attributes.get(SERVICE_INTERFACE_ATTR);
		beanDefinitionBuilder.addConstructorArgValue(serviceInterface);
		beanDefinitionBuilder.addPropertyValue("maxBlock", attributes.get("maxBlock"));

		AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
		beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, serviceInterface);

		String id = (String) attributes.get("name");
		if (!StringUtils.hasText(id)) {
			id = serviceInterface;
		}

		return new BeanDefinitionHolder(beanDefinition, id);
	}
}
