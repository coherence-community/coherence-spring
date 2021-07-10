/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.support;

/**
 * Used by {@link SessionDebugMessageUtils}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public enum SessionEvent {
	/**
	 * The session was created.
	 */
	CREATED,
	/**
	 * The session was deleted.
	 */
	DELETED,
	/**
	 * The session has expired.
	 */
	EXPIRED
}
