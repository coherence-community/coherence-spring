/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.Console;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.processor.AbstractProcessor;

import org.springframework.session.MapSession;

/**
 * Remote fixture state and deliberately denied types; never packaged in the library.
 */
final class SessionTestSupport {

	private SessionTestSupport() {
	}

	static Option[] serverOptions(Option... options) {
		List<Option> properties = new ArrayList<>(Arrays.asList(options));
		properties.add(Console.of(new SessionRejectionEvidence()));
		for (String name : new String[] { "coherence.security.mode", "coherence.remote.dynamic.unauthenticated",
				"coherence.log", "java.io.tmpdir" }) {
			String value = System.getProperty(name);
			if (value != null) {
				properties.add(SystemProperty.of(name, value));
			}
		}
		return properties.toArray(Option[]::new);
	}

	static boolean hasSecurityPolicy() {
		try {
			Class.forName("com.tangosol.io.internal.SerializationAllowlist");
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

	public static class EndpointState implements RemoteCallable<Map<String, String>> {
		@Override
		public Map<String, String> call() throws Exception {
			Map<String, String> state = new HashMap<>();
			for (String name : new String[] { "coherence.security.mode", "coherence.remote.dynamic.unauthenticated",
					"java.version", "coherence.grpc.serializer.allowlist", "java.io.tmpdir", "coherence.log" }) {
				state.put(name, System.getProperty(name));
			}
			state.put("serializer", CacheFactory.getCache(CoherenceIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME)
					.getCacheService().getSerializer().getName());
			state.put("payloadRead", Integer.toString(UnlistedPayload.readCount));
			state.put("processorInvoked", Integer.toString(UnclassifiedProcessor.invocationCount));
			var resources = SessionResetEntryProcessor.class.getClassLoader()
					.getResources("META-INF/coherence/security-config.xml");
			boolean found = false;
			while (resources.hasMoreElements()) {
				try (var stream = resources.nextElement().openStream()) {
					String xml = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
					found |= xml.contains("com.oracle.coherence.spring.session.SessionResetEntryProcessor");
				}
			}
			state.put("sessionPolicy", Boolean.toString(found));
			return state;
		}
	}

	public static class UnlistedPayload implements Serializable {

		static int readCount;

		private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
			readCount++;
			stream.defaultReadObject();
		}
	}

	public static class PayloadState implements RemoteCallable<Map<String, String>> {

		private final String cacheName;

		private final String sessionId;

		private final boolean cleanup;

		PayloadState(String cacheName, String sessionId, boolean cleanup) {
			this.cacheName = cacheName;
			this.sessionId = sessionId;
			this.cleanup = cleanup;
		}

		@Override
		public Map<String, String> call() {
			var cache = CacheFactory.<String, MapSession>getCache(this.cacheName);
			MapSession session = cache.get(this.sessionId);
			Map<String, String> state = Map.of("present", Boolean.toString(session != null),
					"attributes", Integer.toString((session != null) ? session.getAttributeNames().size() : -1),
					"payloadRead", Integer.toString(UnlistedPayload.readCount));
			if (this.cleanup) {
				cache.destroy();
			}
			return state;
		}
	}

	public static class UnclassifiedProcessor extends AbstractProcessor<String, MapSession, Object>
			implements PortableObject {

		static int invocationCount;

		@Override
		public Object process(InvocableMap.Entry<String, MapSession> entry) {
			invocationCount++;
			entry.getValue().setAttribute("unclassifiedExecuted", true);
			entry.setValue(entry.getValue());
			return null;
		}

		@Override
		public void readExternal(PofReader reader) {
		}

		@Override
		public void writeExternal(PofWriter writer) {
		}
	}
}
