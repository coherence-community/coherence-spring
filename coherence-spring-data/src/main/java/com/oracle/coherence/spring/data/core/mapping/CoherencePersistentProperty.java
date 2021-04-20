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
package com.oracle.coherence.spring.data.core.mapping;

import javax.persistence.Id;

import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;

/**
 * Coherence implementation of {@link PersistentProperty}.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherencePersistentProperty
		extends AnnotationBasedPersistentProperty<CoherencePersistentProperty> {

	public CoherencePersistentProperty(Property property,
			PersistentEntity<?, CoherencePersistentProperty> owner, SimpleTypeHolder simpleTypeHolder) {

		super(property, owner, simpleTypeHolder);
	}

	@Override
	protected Association<CoherencePersistentProperty> createAssociation() {
		return new Association<>(this, null);
	}

	@Override
	public boolean isIdProperty() {
		return findAnnotation(Id.class) != null;
	}
}
