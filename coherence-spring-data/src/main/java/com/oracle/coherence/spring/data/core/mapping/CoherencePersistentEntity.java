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
