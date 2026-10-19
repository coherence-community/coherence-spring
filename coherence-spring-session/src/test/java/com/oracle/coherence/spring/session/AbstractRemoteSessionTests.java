/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.io.InvalidClassException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.coherence.common.base.Logger;
import com.oracle.coherence.grpc.client.common.NamedCacheClient;
import com.tangosol.coherence.component.util.SafeNamedCache;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ExternalizableHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.session.MapSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests the actual serializer and remote installation boundaries.
 */
abstract class AbstractRemoteSessionTests extends AbstractCoherenceIndexedSessionRepositoryTests {

	protected abstract CoherenceClusterMember getServer();

	protected String expectedSerializer() {
		return "java";
	}

	private String expectedRoute() {
		return getSessionCache().getCacheService().getInfo().getServiceType().equals("RemoteCache") ? "EXTEND_PROXY" : "GRPC";
	}

	@Test
	void verifiesRemoteConfiguration() {
		NamedCache<String, MapSession> cache = getSessionCache();
		assertThat(cache.getCacheService().getSerializer().getName()).isEqualTo(expectedSerializer());
		Map<String, String> server = getServer().invoke(new SessionTestSupport.EndpointState());
		assertThat(server.get("serializer")).isEqualTo(expectedSerializer());
		assertThat(server.get("sessionPolicy")).isEqualTo("true");
		for (String property : new String[] { "coherence.security.mode", "coherence.remote.dynamic.unauthenticated",
				"java.io.tmpdir", "coherence.log", "java.version" }) {
			assertThat(server.get(property)).as(property).isEqualTo(System.getProperty(property));
		}
		NamedCache actual = cache.getCacheService().ensureCache(cache.getCacheName(), null);
		while (actual instanceof SafeNamedCache safe) {
			actual = safe.getNamedCache();
		}
		if (actual instanceof NamedCacheClient client) {
			int version = client.getAsyncClient().getClientProtocol().getVersion();
			assertThat(version).as("gRPC protocol; legacy fallback must not be used").isGreaterThan(0);
			Logger.info("SESSION ENDPOINT protocol=" + version + " clientSerializer=" + expectedSerializer() + " server=" + server);
		}
		else {
			assertThat(cache.getCacheService().getInfo().getServiceType()).isEqualTo("RemoteCache");
			Logger.info("SESSION ENDPOINT protocol=Extend clientSerializer=" + expectedSerializer() + " server=" + server);
		}
	}

	@Test
	@EnabledIf("securityPolicyAvailable")
	void rejectsUnlistedSessionAttributeBeforeReadingIt() throws Exception {
		NamedCache<String, MapSession> cache = getSessionCache();
		var serializer = cache.getCacheService().getSerializer();
		Class<?> allowlist = Class.forName("com.tangosol.io.internal.SerializationAllowlist");
		assertThat(allowlist.getMethod("isAllowlisted", Class.class)
				.invoke(null, SessionTestSupport.UnlistedPayload.class)).isEqualTo(false);
		var rejection = assertThatThrownBy(() -> ExternalizableHelper.fromBinary(ExternalizableHelper.toBinary(
				new SessionTestSupport.UnlistedPayload(), serializer), serializer));
		if (expectedSerializer().equals("pof")) {
			rejection.hasRootCauseInstanceOf(IllegalArgumentException.class).rootCause()
					.hasMessageStartingWith("unknown user type: " + SessionTestSupport.UnlistedPayload.class.getName() + ".");
		}
		else {
			rejection.hasRootCauseInstanceOf(InvalidClassException.class);
		}
		assertThat(SessionTestSupport.UnlistedPayload.readCount).isZero();
		for (String location : new String[] { "attribute", "principal", "details" }) {
			MapSession session = new MapSession();
			SessionTestSupport.UnlistedPayload payload = new SessionTestSupport.UnlistedPayload();
			if (location.equals("attribute")) {
				session.setAttribute("applicationOwned", payload);
			}
			else {
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
						location.equals("principal") ? payload : "user", "password");
				if (location.equals("details")) {
					token.setDetails(payload);
				}
				session.setAttribute(SPRING_SECURITY_CONTEXT, new SecurityContextImpl(token));
			}
			Map<String, Object> delta = new HashMap<>();
			for (String name : session.getAttributeNames()) {
				delta.put(name, session.getAttribute(name));
			}
			SessionUpdateEntryProcessor update = new SessionUpdateEntryProcessor();
			update.setDelta(delta);
			String cacheName = "session-payload-control-" + location + "-" + UUID.randomUUID();
			NamedCache<String, MapSession> control = cache.getCacheService().ensureCache(cacheName, null);
			control.put(session.getId(), new MapSession(session.getId()));
			try {
				if (expectedSerializer().equals("pof")) {
					// This graph is intentionally unregistered: POF rejects it at the writer,
					// before transmission, rather than at the receiver's Java allowlist.
					assertThatThrownBy(() -> control.invoke(session.getId(), update)).as(location)
							.hasRootCauseInstanceOf(IllegalArgumentException.class).rootCause()
							.hasMessageStartingWith("unknown user type: " + SessionTestSupport.UnlistedPayload.class.getName() + ".");
				}
				else {
					// The filter exception does not name the denied class, and gRPC
					// sanitizes its cause entirely. Require the receiver's exact event.
					Throwable failure = SessionRejectionEvidence.assertRejected(getServer(),
							() -> control.invoke(session.getId(), update), expectedRoute(), "filter",
							SessionTestSupport.UnlistedPayload.class, "serialization-allowlist-rejected");
					if (expectedRoute().equals("EXTEND_PROXY")) {
						assertThat(failure).hasRootCauseInstanceOf(InvalidClassException.class)
								.hasRootCauseMessage("filter status: REJECTED");
					}
				}
				assertThat(getServer().invoke(new SessionTestSupport.PayloadState(cacheName, session.getId(), false)))
						.containsEntry("present", "true").containsEntry("attributes", "0").containsEntry("payloadRead", "0");
			}
			finally {
				getServer().invoke(new SessionTestSupport.PayloadState(cacheName, session.getId(), true));
			}
		}
	}

	@Test
	@EnabledIf("securityPolicyAvailable")
	void rejectsAllowedButUnclassifiedNamedProcessorBeforeInvocation() throws Exception {
		Class<?> allowlist = Class.forName("com.tangosol.io.internal.SerializationAllowlist");
		assertThat(allowlist.getMethod("isAllowlisted", Class.class)
				.invoke(null, SessionTestSupport.UnclassifiedProcessor.class)).isEqualTo(true);
		Class<?> policyType = Class.forName("com.tangosol.util.RemoteExecutablePolicy");
		Object policy = policyType.getMethod("current").invoke(null);
		assertThat(policyType.getMethod("isExecutable", Class.class)
				.invoke(policy, SessionTestSupport.UnclassifiedProcessor.class)).isEqualTo(false);
		NamedCache<String, MapSession> cache = getSessionCache();
		var serializer = cache.getCacheService().getSerializer();
		// A writer failure must not masquerade as receiver executable-policy rejection.
		assertThatCode(() -> assertThat(ExternalizableHelper.<Object>fromBinary(ExternalizableHelper.toBinary(
				new SessionTestSupport.UnclassifiedProcessor(), serializer), serializer))
				.isInstanceOf(SessionTestSupport.UnclassifiedProcessor.class))
				.as("named processor must serialize before testing receiver executable policy").doesNotThrowAnyException();
		MapSession session = new MapSession();
		cache.put(session.getId(), session);
		try {
			Throwable failure = SessionRejectionEvidence.assertRejected(getServer(),
					() -> cache.invoke(session.getId(), new SessionTestSupport.UnclassifiedProcessor()),
					expectedRoute(), "remote-executable-policy", SessionTestSupport.UnclassifiedProcessor.class, "PROCESS_ENTRY");
			if (expectedRoute().equals("EXTEND_PROXY")) {
				assertThat(failure).isInstanceOf(SecurityException.class)
						.hasMessage("Remote execution denied for class " + SessionTestSupport.UnclassifiedProcessor.class.getName()
								+ " (reason=PROCESS_ENTRY, role=EXTEND_PROXY); class is not annotated with @Remote.Executable"
								+ " and is not listed as executable=\"true\" in any merged security-config.xml");
			}
			assertThat(cache.get(session.getId()).getAttributeNames()).doesNotContain("unclassifiedExecuted");
			assertThat(getServer().invoke(new SessionTestSupport.EndpointState()).get("processorInvoked")).isEqualTo("0");
		}
		finally {
			cache.remove(session.getId());
		}
	}

	static boolean securityPolicyAvailable() {
		return SessionTestSupport.hasSecurityPolicy();
	}
}
