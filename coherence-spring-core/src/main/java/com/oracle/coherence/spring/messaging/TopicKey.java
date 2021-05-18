/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.util.Objects;

/**
 * A simple key to a {@link com.tangosol.net.topic.Publisher}.
 *
 * @author Jonathan Knight
 * @since 3.0
 */
class TopicKey {
	/**
	 * The name of the topic.
	 */
	private final String topicName;

	/**
	 * The name of the owning session.
	 */
	private final String sessionName;

	/**
	 * Create a {@link TopicKey}.
	 *
	 * @param topicName     the name of the topic
	 * @param sessionName   the name of the owning session
	 */
	TopicKey(String topicName, String sessionName) {
		this.topicName = topicName;
		this.sessionName = sessionName;
	}

	/**
	 * Returns the topic name.
	 * @return the topic name
	 */
	String getTopicName() {
		return this.topicName;
	}

	/**
	 * Returns the session name.
	 * @return the session name
	 */
	String getSessionName() {
		return this.sessionName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TopicKey that = (TopicKey) o;
		return Objects.equals(this.topicName, that.topicName) &&
				Objects.equals(this.sessionName, that.sessionName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.topicName, this.sessionName);
	}
}
