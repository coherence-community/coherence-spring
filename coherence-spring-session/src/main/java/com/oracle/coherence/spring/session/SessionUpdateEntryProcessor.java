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
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;

import org.springframework.session.MapSession;

/**
 * Coherence {@link com.tangosol.util.InvocableMap.EntryProcessor} responsible for handling updates to session.
 *
 * @author Gunnar Hillert
 * @since 3.0
 * @see com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository#save(CoherenceSpringSession)
 */
public class SessionUpdateEntryProcessor extends AbstractProcessor<String, MapSession, Object>
		implements PortableObject {

	private Instant lastAccessedTime;

	private Duration maxInactiveInterval;

	private Map<String, Object> delta;

	@Override
	public Object process(InvocableMap.Entry<String, MapSession> entry) {
		final MapSession value = entry.getValue();

		if (value == null) {
			return Boolean.FALSE;
		}
		if (this.lastAccessedTime != null) {
			value.setLastAccessedTime(this.lastAccessedTime);
		}
		if (this.maxInactiveInterval != null) {
			value.setMaxInactiveInterval(this.maxInactiveInterval);
			final BinaryEntry binaryEntry = MemcachedHelper.getBinaryEntry(entry);
			final long maxInactiveIntervalMillis = this.maxInactiveInterval.toMillis();

			if (maxInactiveIntervalMillis > 0) {
				binaryEntry.expire(maxInactiveIntervalMillis);
			}
			else {
				binaryEntry.expire(CacheMap.EXPIRY_NEVER);
			}
		}
		if (this.delta != null) {
			for (final Map.Entry<String, Object> attribute : this.delta.entrySet()) {
				if (attribute.getValue() != null) {
					value.setAttribute(attribute.getKey(), attribute.getValue());
				}
				else {
					value.removeAttribute(attribute.getKey());
				}
			}
		}
		entry.setValue(value, false);
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

	void setDelta(Map<String, Object> delta) {
		this.delta = delta;
	}

	@Override
	public void readExternal(PofReader pofReader) throws IOException {
		this.lastAccessedTime = pofReader.readObject(0);
		this.maxInactiveInterval = pofReader.readObject(1);
		this.delta = pofReader.readMap(2, new HashMap<>());
	}

	@Override
	public void writeExternal(PofWriter pofWriter) throws IOException {
		pofWriter.writeObject(0, this.lastAccessedTime);
		pofWriter.writeObject(1, this.maxInactiveInterval);
		pofWriter.writeMap(2, this.delta);
	}
}
