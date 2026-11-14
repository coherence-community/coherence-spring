/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.Serializer;
import com.tangosol.io.pof.ConfigurablePofContext;
import com.tangosol.util.BinaryEntry;
import com.tangosol.util.ExternalizableHelper;
import org.junit.jupiter.api.Test;

import org.springframework.session.MapSession;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behavioral and wire-contract regressions for the named reset processor.
 */
class SessionResetEntryProcessorTests {

	@Test
	void preservesJavaAndPofIntervalsAndExpirySentinels() {
		for (Serializer serializer : List.of(new DefaultSerializer(), new ConfigurablePofContext("pof-config.xml"))) {
			for (long interval : new long[] { 1234L, 0L, -1L }) {
				SessionResetEntryProcessor processor = ExternalizableHelper.fromBinary(
						ExternalizableHelper.toBinary(new SessionResetEntryProcessor(interval), serializer), serializer);
				MapSession session = new MapSession();
				Instant accessed = session.getLastAccessedTime();
				session.setAttribute("retained", "value");
				List<Object> mutations = new ArrayList<>();
				assertThat(processor.process(entry(session, mutations))).isNull();
				assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofMillis(interval));
				assertThat(session.getLastAccessedTime()).isEqualTo(accessed);
				assertThat(session.<String>getAttribute("retained")).isEqualTo("value");
				assertThat(mutations).containsExactly(interval, session);
			}
		}
	}

	@Test
	void doesNotReviveExpiredSessions() {
		MapSession session = new MapSession();
		session.setMaxInactiveInterval(Duration.ofSeconds(1));
		session.setLastAccessedTime(Instant.now().minusSeconds(60));
		List<Object> mutations = new ArrayList<>();
		assertThat(new SessionResetEntryProcessor(60000).process(entry(session, mutations))).isNull();
		assertThat(session.getMaxInactiveInterval()).isEqualTo(Duration.ofSeconds(1));
		assertThat(mutations).isEmpty();
	}

	@Test
	void ignoresAbsentEntries() {
		List<Object> mutations = new ArrayList<>();
		assertThat(new SessionResetEntryProcessor(60000).process(entry(null, mutations))).isNull();
		assertThat(mutations).isEmpty();
	}

	@SuppressWarnings("unchecked")
	private BinaryEntry<String, MapSession> entry(MapSession session, List<Object> mutations) {
		return (BinaryEntry<String, MapSession>) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { BinaryEntry.class }, (proxy, method, args) -> {
					if (method.getName().equals("getValue")) {
						return session;
					}
					if (method.getName().equals("expire") || method.getName().equals("setValue")) {
						mutations.add(args[0]);
						return null;
					}
					throw new AssertionError("Unexpected entry operation: " + method);
				});
	}
}
