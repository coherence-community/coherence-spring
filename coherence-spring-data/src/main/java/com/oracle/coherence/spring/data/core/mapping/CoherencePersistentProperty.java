/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
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
