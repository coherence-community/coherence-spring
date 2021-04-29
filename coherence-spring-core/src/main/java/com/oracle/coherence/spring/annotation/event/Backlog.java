/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation.event;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A qualifier annotation used for any BACKLOG event.
 *
 * @author Aleks Seovic
 * @author Gunnar Hillert
 * @since 3.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Backlog {
	/**
	 * Obtain the type of backlog event.
	 *
	 * @return the type of backlog event
	 */
	Type value();

	/**
	 * The backlog event type.
	 */
	enum Type {
		/**
		 * Indicates that a participant was previously
		 * backlogged but is no longer so.
		 */
		NORMAL,

		/**
		 * Indicates that a participant is backlogged; if
		 * the participant is remote it indicates the
		 * remote participant has more work than it can handle;
		 * if the participant is local it indicates this
		 * participant has more work than it can handle.
		 */
		EXCESSIVE
	}

}
