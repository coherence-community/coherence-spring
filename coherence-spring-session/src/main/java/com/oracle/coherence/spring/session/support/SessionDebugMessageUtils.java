/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.support;

import org.springframework.session.MapSession;
import org.springframework.util.Assert;

/**
 * Contains utilities to create common log messages.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public final class SessionDebugMessageUtils {
	private SessionDebugMessageUtils() {
		throw new AssertionError("Utility Class.");
	}

	/**
	 * Create a common log message for session events.
	 * @param event must not be null
	 * @param session must not be null
	 * @return log message.
	 */
	public static String createSessionEventMessage(SessionEvent event, MapSession session) {
		Assert.notNull(event, "SessionEvent must not be null");
		Assert.notNull(session, "MapSession must not be null");

		return String.format("Session %s [Id: %s; created on %s; "
				+ "session timeout: %ss; last accessed: %s]",
			event.name(), session.getId(), session.getCreationTime(),
			session.getMaxInactiveInterval().getSeconds(), session.getLastAccessedTime());
	}
}
