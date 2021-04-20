/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.coherence.spring.data.config;

import java.lang.annotation.Annotation;

import com.oracle.coherence.spring.data.support.CoherenceRepositoryConfigurationExtension;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * This class wil register support for Coherence repositories with the Spring runtime.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherenceRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {
	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableCoherenceRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new CoherenceRepositoryConfigurationExtension();
	}
}
