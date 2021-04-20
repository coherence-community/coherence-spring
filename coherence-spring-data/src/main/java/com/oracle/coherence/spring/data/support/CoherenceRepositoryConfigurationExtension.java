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
package com.oracle.coherence.spring.data.support;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.Entity;

import com.oracle.coherence.spring.data.repository.CoherenceRepository;

import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;

/**
 * Coherence implementation of {@link RepositoryConfigurationExtensionSupport}.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherenceRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport {
	@Override
	public String getRepositoryFactoryBeanClassName() {
		return CoherenceRepositoryFactoryBean.class.getName();
	}

	@Override
	public String getModuleName() {
		return "Coherence";
	}

	@Override
	protected String getModulePrefix() {
		return "coherence";
	}

	@Override
	protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
		return Collections.singleton(Entity.class);
	}

	@Override
	protected Collection<Class<?>> getIdentifyingTypes() {
		return Collections.singleton(CoherenceRepository.class);
	}
}
