/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.support;

import org.springframework.session.SessionIdGenerator;

/**
 * Example of a custom {@link SessionIdGenerator}.
 *
 * @author Gunnar Hillert
 */
public class FixedSessionIdGenerator implements SessionIdGenerator {

	private final String id;

	public FixedSessionIdGenerator(String id) {
		this.id = id;
	}

	@Override
	public String generate() {
		return this.id;
	}

}
