/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging.exceptions;

import java.util.Optional;

import com.tangosol.net.topic.Subscriber;

/**
 * Exception thrown for subscriber errors.
 *
 * @author Jonathan Knight
 * @since 3.0
 */
public class CoherenceSubscriberException extends RuntimeException {

	private final Object listener;
	private final Subscriber<?> kafkaConsumer;
	private final Subscriber.Element<?> element;

	/**
	 * Creates a new exception.
	 *
	 * @param message the message
	 * @param listener the listener
	 * @param kafkaConsumer the consumer
	 * @param element the consumer record
	 */
	public CoherenceSubscriberException(String message, Object listener, Subscriber<?> kafkaConsumer, Subscriber.Element<?> element) {
		super(message);
		this.listener = listener;
		this.kafkaConsumer = kafkaConsumer;
		this.element = element;
	}

	/**
	 * Creates a new exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 * @param listener the listener
	 * @param kafkaConsumer the consumer
	 * @param element the consumer record
	 */
	public CoherenceSubscriberException(String message, Throwable cause, Object listener, Subscriber<?> kafkaConsumer, Subscriber.Element<?> element) {
		super(message, cause);
		this.listener = listener;
		this.kafkaConsumer = kafkaConsumer;
		this.element = element;
	}

	/**
	 * Creates a new exception.
	 *
	 * @param cause the cause
	 * @param listener the listener
	 * @param kafkaConsumer the consumer
	 * @param element the consumer record
	 */
	public CoherenceSubscriberException(Throwable cause, Object listener, Subscriber<?> kafkaConsumer, Subscriber.Element<?> element) {
		super(cause.getMessage(), cause);
		this.listener = listener;
		this.kafkaConsumer = kafkaConsumer;
		this.element = element;
	}

	/**
	 * Return The bean that is the kafka listener.
	 * @return the bean that is the kafka listener
	 */
	public Object getKafkaListener() {
		return this.listener;
	}

	/**
	 * Return the consumer that produced the error.
	 * @return the consumer that produced the error
	 */
	public Subscriber<?> getKafkaConsumer() {
		return this.kafkaConsumer;
	}

	/**
	 * Return the element that was being processed that caused the error.
	 * @return the element that was being processed that caused the error
	 */
	public Optional<Subscriber.Element<?>> getElement() {
		return Optional.ofNullable(this.element);
	}
}
