/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SessionRejectionEvidenceTests {

	private static final String EVENT = "route=EXTEND_PROXY|gate=filter|principal=-|denied-class="
			+ SessionTestSupport.UnlistedPayload.class.getName() + "|reason=serialization-allowlist-rejected";

	@Test
	void acceptsTheCompleteReceiverRejection() {
		assertPayloadRejection(List.of("2026-10-15 <Warning> (thread=receiver, member=1): " + EVENT));
	}

	@Test
	void rejectsOtherRoutesClassesGatesAndReasons() {
		for (String wrong : List.of(EVENT.replace("EXTEND_PROXY", "GRPC"),
				EVENT.replace("gate=filter", "gate=remote-executable-policy"),
				EVENT.replace("UnlistedPayload", "UnlistedPayloadOther"),
				EVENT.replace("serialization-allowlist-rejected", "some-other-reason"))) {
			assertThatThrownBy(() -> assertPayloadRejection(List.of(wrong))).isInstanceOf(AssertionError.class);
		}
	}

	@Test
	void rejectsMissingDuplicateAndEmbeddedEvidence() {
		for (List<String> lines : List.of(List.<String>of(), List.of(EVENT, EVENT),
				List.of("exception included " + EVENT), List.of(EVENT + " extra text"))) {
			assertThatThrownBy(() -> assertPayloadRejection(lines)).isInstanceOf(AssertionError.class);
		}
	}

	@Test
	void excludesStaleAndSubsequentEvents() {
		try (SessionRejectionEvidence evidence = new SessionRejectionEvidence()) {
			evidence.begin("first");
			evidence.record(EVENT);
			evidence.record("SESSION REJECTION BEGIN first");
			evidence.record(EVENT);
			evidence.record("SESSION REJECTION END first");
			evidence.record(EVENT);
			assertPayloadRejection(evidence.snapshot());
			evidence.begin("second");
			evidence.record(EVENT);
			evidence.record("SESSION REJECTION BEGIN first");
			evidence.record(EVENT);
			evidence.record("SESSION REJECTION BEGIN second");
			evidence.record("unrelated request failed");
			evidence.record("SESSION REJECTION END second");
			evidence.record(EVENT);
			assertThat(evidence.snapshot()).containsExactly("unrelated request failed");
			assertThatThrownBy(() -> assertPayloadRejection(evidence.snapshot())).isInstanceOf(AssertionError.class);
		}
	}

	private void assertPayloadRejection(List<String> lines) {
		SessionRejectionEvidence.assertRejection(lines, "EXTEND_PROXY", "filter",
				SessionTestSupport.UnlistedPayload.class, "serialization-allowlist-rejected");
	}
}
