package com.oracle.coherence.spring;

import com.tangosol.net.NamedCache;

public class CoherenceInstance {

	private SpringBasedCoherenceSession springBasedCoherenceSession;

	public CoherenceInstance(SpringBasedCoherenceSession springBasedCoherenceSession) {
		super();
		this.springBasedCoherenceSession = springBasedCoherenceSession;
		System.out.println("Starting Coherence Instance.");


		springBasedCoherenceSession.getCache("cc");
	}

	private <K, V> NamedCache<K, V> getCache(String cacheName) {
		return this.springBasedCoherenceSession.getCache(cacheName);
	}

}
