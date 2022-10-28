/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MapEventTransformer;
import data.Person;

/**
 * A custom implementation of a {@link MapEventTransformer}.
 * @author Gunnar Hillert
 */
class UppercaseNameTransformer implements MapEventTransformer<String, Person, String> {

	@Override
	@SuppressWarnings("unchecked")
	public MapEvent<String, String> transform(MapEvent<String, Person> event) {
		String sOldName = transform(event.getOldValue());
		String sNewName = transform(event.getNewValue());
		return new MapEvent<String, String>(event.getMap(), event.getId(), event.getKey(), sOldName, sNewName);
	}

	String transform(Person person) {
		if (person == null) {
			return null;
		}
		String name = person.getFirstName();
		return (name != null) ? name.toUpperCase() : null;
	}
}
