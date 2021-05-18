/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

/**
 * <p>An enum representing different strategies for committing positions in a Coherence topic when
 * using {@link CoherenceTopicListener}.</p>
 * <p>To track messages that have been consumed, Coherence allows committing positions at a frequency desired by
 * the developer.</p>
 * <p>Depending on requirements you may wish the commit more or less frequently and you may not care whether the
 * commit was successful or not. This enum allows configuring a range of policies for a Coherence topic subscriber
 * from leaving it down to the client to synchronously commit (with {@link #SYNC}) or asynchronously commit
 * (with {@link #ASYNC}) after each message is consumed, through to manually handling commits (with {@link #MANUAL}).</p>
 *
 * @author Jonathan Knight
 * @since 3.0
 */
public enum CommitStrategy {
	/**
	 * Do not commit messages. In this case the subscriber method should accept an argument that is the {@link com.tangosol.net.topic.Subscriber.Element}
	 * itself and call {@link com.tangosol.net.topic.Subscriber.Element#commit()} or {@link com.tangosol.net.topic.Subscriber.Element#commitAsync()}
	 * to commit the received element.
	 */
	MANUAL,
	/**
	 * Synchronously commit using {@link com.tangosol.net.topic.Subscriber.Element#commit()} after each messages is processed.
	 */
	SYNC,
	/**
	 * Asynchronously commit using {@link com.tangosol.net.topic.Subscriber.Element#commitAsync()} after each messages is processed.
	 */
	ASYNC,
}
