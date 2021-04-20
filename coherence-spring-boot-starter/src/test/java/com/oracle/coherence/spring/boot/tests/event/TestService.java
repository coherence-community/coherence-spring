/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 *
 * @author Gunnar Hillert
 *
 */
@Service
public class TestService {
	final List<String> eventNames = Collections.synchronizedList(new ArrayList<>());

	public void addEventName(String eventName) {
		this.eventNames.add(eventName);
	}

	public List<String> getEventNames() {
		return this.eventNames;
	}

}
