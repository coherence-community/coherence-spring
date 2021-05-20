/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.serialization.pof;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

import org.springframework.session.MapSession;

public class MapSessionPofSerializer implements PofSerializer {
	@Override
	public void serialize(PofWriter pofWriter, Object value) throws IOException {
		final MapSession mapSession = (MapSession) value;

		pofWriter.writeString(0, mapSession.getId());
		pofWriter.writeObject(1, mapSession.getCreationTime());
		pofWriter.writeObject(2, mapSession.getMaxInactiveInterval());
		pofWriter.writeObject(3, mapSession.getLastAccessedTime());

		final Map<String, Object> sessionAttributes = new HashMap<>(mapSession.getAttributeNames().size());
		for (String attrName : mapSession.getAttributeNames()) {
			Object attrValue = mapSession.getAttribute(attrName);
			if (attrValue != null) {
				sessionAttributes.put(attrName, attrValue);
			}
		}
		pofWriter.writeMap(5, sessionAttributes, String.class);
		pofWriter.writeRemainder(null);
	}

	@Override
	public Object deserialize(PofReader pofReader) throws IOException {
		final String id = pofReader.readString(0);
		final Instant creationTime = pofReader.readObject(1);
		final Duration maxInactiveInterval = pofReader.readObject(2);
		final Instant lastAccessedTime = pofReader.readObject(3);
		final Map<String, Object> attributes = pofReader.readMap(5, new ConcurrentHashMap<>());
		pofReader.readRemainder();

		final MapSession mapSession = new MapSession(id);
		mapSession.setCreationTime(creationTime);
		mapSession.setMaxInactiveInterval(maxInactiveInterval);
		mapSession.setLastAccessedTime(lastAccessedTime);

		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			mapSession.setAttribute(entry.getKey(), entry.getValue());
		}

		return mapSession;
	}
}
