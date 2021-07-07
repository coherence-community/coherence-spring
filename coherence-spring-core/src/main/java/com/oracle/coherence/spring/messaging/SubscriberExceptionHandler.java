/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import com.oracle.coherence.spring.messaging.exceptions.CoherenceSubscriberException;

/**
 * Interface that {@link com.oracle.coherence.spring.annotation.CoherenceTopicListener} beans can implement to handle exceptions.
 *
 * @author Jonathan Knight
 * @since 3.0
 */
public interface SubscriberExceptionHandler {
	/**
	 * Handle the given exception.
	 * @param exception the exception to handle
	 * @return {@code true} to continue processing messages, or {@code false} to close the subscriber.
	 *
	 */
	Action handle(CoherenceSubscriberException exception);

	/**
	 * An enumeration of possible actions to take after handling an exception.
	 */
	enum Action {
		/**
		 * Continue to receive further messages.
		 */
		Continue,
		/**
		 * Close the subscriber and stop receiving messages.
		 */
		Stop
	}
}
