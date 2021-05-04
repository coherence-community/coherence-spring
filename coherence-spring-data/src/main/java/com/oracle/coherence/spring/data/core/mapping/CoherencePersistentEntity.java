/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.core.mapping;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.util.TypeInformation;

/**
 * Coherence implementation of {@link PersistentEntity}.
 *
 * @param <T> the entity type
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherencePersistentEntity<T>
		extends BasicPersistentEntity<T, CoherencePersistentProperty> {

	public CoherencePersistentEntity(TypeInformation<T> information) {
		super(information);
	}
}
