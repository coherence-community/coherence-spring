/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * http://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import com.tangosol.net.NamedCache;

/**
 *
 * @author Gunnar Hillert
 *
 */
public class CoherenceInstance {

	private SpringBasedCoherenceSession springBasedCoherenceSession;

	public CoherenceInstance(SpringBasedCoherenceSession springBasedCoherenceSession) {
		super();
		this.springBasedCoherenceSession = springBasedCoherenceSession;
	}

	private <K, V> NamedCache<K, V> getCache(String cacheName) {
		return this.springBasedCoherenceSession.getCache(cacheName);
	}

}
