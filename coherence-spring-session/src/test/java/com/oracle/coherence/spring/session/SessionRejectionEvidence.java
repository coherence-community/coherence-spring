/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.oracle.bedrock.runtime.ApplicationConsoleBuilder;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.console.EventsApplicationConsole;
import com.oracle.coherence.common.base.Logger;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.awaitility.Awaitility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Captures receiver evidence for one synchronous request between fresh log markers.
 */
final class SessionRejectionEvidence extends EventsApplicationConsole {

	private final List<String> lines = new ArrayList<>();

	private String begin;

	private String end;

	private boolean started;

	private boolean finished;

	SessionRejectionEvidence() {
		withStdOutListener((line) -> {
			System.out.println(line);
			record(line);
		});
		withStdErrListener((line) -> {
			System.err.println(line);
			record(line);
		});
	}

	static Throwable assertRejected(CoherenceClusterMember server, ThrowingCallable operation,
			String route, String gate, Class<?> type, String reason) {
		SessionRejectionEvidence evidence = (SessionRejectionEvidence) server.getOptions()
				.get(ApplicationConsoleBuilder.class).build(server.getName());
		String token = UUID.randomUUID().toString();
		evidence.begin(token);
		server.invoke(new Marker(evidence.begin));
		Awaitility.await().atMost(Duration.ofSeconds(10)).until(evidence::hasStarted);
		Throwable failure;
		try {
			failure = catchThrowable(operation);
		}
		finally {
			// The receiver queues its rejection before completing the request. The end
			// marker uses that same logger, so delayed console delivery cannot leak
			// evidence into the next request's window.
			server.invoke(new Marker(evidence.end));
			Awaitility.await().atMost(Duration.ofSeconds(10)).until(evidence::hasFinished);
		}
		assertThat(failure).as("request must fail").isInstanceOf(RuntimeException.class);
		assertRejection(evidence.snapshot(), route, gate, type, reason);
		return failure;
	}

	static void assertRejection(List<String> lines, String route, String gate, Class<?> type, String reason) {
		String expected = "route=" + route + "|gate=" + gate + "|principal=-|denied-class=" + type.getName() + "|reason=" + reason;
		assertThat(lines).as("receiver rejection for this request: %s", expected)
				.filteredOn((line) -> isMessage(line, expected)).hasSize(1);
	}

	// Match a complete structured message, never a stack trace or substring of
	// another denial. Coherence's default console prefix ends with "): ".
	private static boolean isMessage(String line, String message) {
		return line.equals(message) || line.endsWith("): " + message);
	}

	synchronized void begin(String token) {
		this.begin = "SESSION REJECTION BEGIN " + token;
		this.end = "SESSION REJECTION END " + token;
		this.started = false;
		this.finished = false;
		this.lines.clear();
	}

	synchronized void record(String line) {
		if (this.begin != null && isMessage(line, this.begin)) {
			this.started = true;
		}
		else if (this.end != null && isMessage(line, this.end)) {
			this.finished = true;
		}
		else if (this.started && !this.finished) {
			this.lines.add(line);
		}
	}

	private synchronized boolean hasStarted() {
		return this.started;
	}

	private synchronized boolean hasFinished() {
		return this.finished;
	}

	synchronized List<String> snapshot() {
		return List.copyOf(this.lines);
	}

	public static class Marker implements RemoteCallable<Void> {

		private final String message;

		Marker(String message) {
			this.message = message;
		}

		@Override
		public Void call() {
			Logger.warn(this.message);
			return null;
		}
	}
}
