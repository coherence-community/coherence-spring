/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.io.IOException;
import java.time.Duration;

import com.tangosol.coherence.memcached.server.MemcachedHelper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;

import org.springframework.session.MapSession;

/**
 * Resets the inactivity interval and cache expiry of an active session.
 * The only serialized state is the interval in milliseconds. Java serialization
 * uses serial version 1; POF stores the interval as a long at index 0.
 *
 * @author Vaso Putica
 * @since 5.0
 * @see CoherenceIndexedSessionRepository#resetMaxInactiveIntervalForActiveSessions()
 */
public class SessionResetEntryProcessor extends AbstractProcessor<String, MapSession, Object>
		implements PortableObject {

	private static final long serialVersionUID = 1L;

	private long expirationInMillis;

	/**
	 * Constructor for POF deserialization.
	 */
	public SessionResetEntryProcessor() {
	}

	/**
	 * Create a processor using the supplied interval.
	 * @param expirationInMillis the interval in milliseconds, including cache expiry sentinels
	 */
	public SessionResetEntryProcessor(long expirationInMillis) {
		this.expirationInMillis = expirationInMillis;
	}

	@Override
	public Object process(InvocableMap.Entry<String, MapSession> entry) {
		final MapSession session = entry.getValue();
		if (session == null || session.isExpired()) {
			return null;
		}
		session.setMaxInactiveInterval(Duration.ofMillis(this.expirationInMillis));
		MemcachedHelper.getBinaryEntry(entry).expire(this.expirationInMillis);
		entry.setValue(session);
		return null;
	}

	@Override
	public void readExternal(PofReader reader) throws IOException {
		this.expirationInMillis = reader.readLong(0);
	}

	@Override
	public void writeExternal(PofWriter writer) throws IOException {
		writer.writeLong(0, this.expirationInMillis);
	}
}
