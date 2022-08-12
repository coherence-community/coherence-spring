/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.core.mapping;

import com.tangosol.util.Base;

import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.model.AnnotationBasedPersistentProperty;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.util.ClassUtils;

/**
 * Coherence implementation of {@link PersistentProperty}.
 *
 * @author Ryan Lubke
 * @author Gunnar Hillert
 * @since 3.0
 */
public class CoherencePersistentProperty
		extends AnnotationBasedPersistentProperty<CoherencePersistentProperty> {

	/**
	 * Eventually consistent flag indicating that the check for {@code jakarta.persistence} has been
	 * made.
	 */
	private boolean jakartaPersistenceChecked;

	/**
	 * The {@code jakarta.persistence.Id} annotation if found. This is a fallback
	 * to {@link Id}.
	 */
	private Class jakartaPersistenceId;

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
		if (findAnnotation(Id.class) != null) {
			return true;
		}
		else {
			if (!this.jakartaPersistenceChecked) {
				this.jakartaPersistenceChecked = true;
				try {
					this.jakartaPersistenceId =
							ClassUtils.forName("jakarta.persistence.Id", Base.getContextClassLoader());
				}
				catch (ClassNotFoundException ignored) {
				}
			}
			if (this.jakartaPersistenceId != null) {
				return findAnnotation(this.jakartaPersistenceId) != null;
			}
			return false;
		}
	}
}
