/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.core.mapping;

import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;

/**
 * Coherence implementation of {@link AbstractMappingContext}.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherenceMappingContext
		extends AbstractMappingContext<CoherencePersistentEntity<?>,
		CoherencePersistentProperty> {

	@Override
	protected <T> CoherencePersistentEntity<T> createPersistentEntity(
			TypeInformation<T> typeInformation) {

		return new CoherencePersistentEntity<>(typeInformation);
	}

	@Override
	protected CoherencePersistentProperty createPersistentProperty(Property property,
			CoherencePersistentEntity<?> owner, SimpleTypeHolder simpleTypeHolder) {

		return new CoherencePersistentProperty(property, owner, simpleTypeHolder);
	}
}
