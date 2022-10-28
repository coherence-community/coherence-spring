/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import com.oracle.coherence.spring.annotation.MapEventTransformerFactory;
import com.tangosol.util.MapEventTransformer;
import data.Person;

/**
 * @author Gunnar Hillert
 */
@UppercaseName
public class UppercaseTransformerFactory implements MapEventTransformerFactory<UppercaseName, String, Person, String> {
	@Override
	public MapEventTransformer<String, Person, String> create(UppercaseName annotation) {
		return new UppercaseNameTransformer();
	}
}
