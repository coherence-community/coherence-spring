/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.tangosol.coherence.memcached.server.MemcachedHelper;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;

import org.springframework.session.MapSession;

/**
 * Coherence {@link InvocableMap.EntryProcessor} responsible for handling updates to session.
 *
 * @author Gunnar Hillert
 * @since 3.0
 * @see CoherenceIndexedSessionRepository#save(CoherenceSpringSession)
 */
public class SessionUpdateEntryProcessor extends AbstractProcessor<String, MapSession, Object>
		implements PortableObject {

	private Instant lastAccessedTime;

	private Duration maxInactiveInterval;
	private Duration defaultMaxInactiveInterval;
	private Map<String, Object> delta;

	@Override
	public Object process(InvocableMap.Entry<String, MapSession> entry) {
		final MapSession mapSession = entry.getValue();

		if (mapSession == null) {
			return Boolean.FALSE;
		}
		if (this.lastAccessedTime != null) {
			mapSession.setLastAccessedTime(this.lastAccessedTime);
		}

		final BinaryEntry binaryEntry = MemcachedHelper.getBinaryEntry(entry);

		if (this.maxInactiveInterval != null) {
			mapSession.setMaxInactiveInterval(this.maxInactiveInterval);
		}
		if (this.delta != null) {
			for (final Map.Entry<String, Object> attribute : this.delta.entrySet()) {
				if (attribute.getValue() != null) {
					mapSession.setAttribute(attribute.getKey(), attribute.getValue());
				}
				else {
					mapSession.removeAttribute(attribute.getKey());
				}
			}
		}

		entry.setValue(mapSession, false);

		if (this.maxInactiveInterval != null && !this.maxInactiveInterval.isNegative()) {
			binaryEntry.expire(this.maxInactiveInterval.toMillis());
		}
		else if (this.defaultMaxInactiveInterval != null && !this.defaultMaxInactiveInterval.isNegative()) {
			binaryEntry.expire(this.defaultMaxInactiveInterval.toMillis());
		}

		return Boolean.TRUE;
	}

	public static BinaryEntry getBinaryEntry(InvocableMap.Entry entry) {
		try {
			return (BinaryEntry) entry;
		}
		catch (ClassCastException cce) {
			throw new RuntimeException(
					"The MemcachedAcceptor is only supported by the DistributedCache");
		}
	}

	void setLastAccessedTime(Instant lastAccessedTime) {
		this.lastAccessedTime = lastAccessedTime;
	}

	void setMaxInactiveInterval(Duration maxInactiveInterval) {
		this.maxInactiveInterval = maxInactiveInterval;
	}

	public void setDefaultMaxInactiveInterval(Duration defaultMaxInactiveInterval) {
		this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
	}

	void setDelta(Map<String, Object> delta) {
		this.delta = delta;
	}

	@Override
	public void readExternal(PofReader pofReader) throws IOException {
		this.lastAccessedTime = pofReader.readObject(0);
		this.maxInactiveInterval = pofReader.readObject(1);
		this.delta = pofReader.readMap(2, new HashMap<>());
		this.defaultMaxInactiveInterval = pofReader.readObject(3);

	}

	@Override
	public void writeExternal(PofWriter pofWriter) throws IOException {
		pofWriter.writeObject(0, this.lastAccessedTime);
		pofWriter.writeObject(1, this.maxInactiveInterval);
		pofWriter.writeMap(2, this.delta);
		pofWriter.writeObject(3, this.defaultMaxInactiveInterval);
	}

}
