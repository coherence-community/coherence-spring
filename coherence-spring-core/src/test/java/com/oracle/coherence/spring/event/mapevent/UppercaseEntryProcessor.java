/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event.mapevent;

import com.tangosol.util.InvocableMap;
import data.Person;

/**
 * @author Gunnar Hillert
 */
public class UppercaseEntryProcessor
		implements InvocableMap.EntryProcessor<String, Person, Object> {
	@Override
	public Object process(InvocableMap.Entry<String, Person> entry) {
		Person p = entry.getValue();
		p.setLastName(p.getLastName().toUpperCase());
		entry.setValue(p);
		return null;
	}
}
