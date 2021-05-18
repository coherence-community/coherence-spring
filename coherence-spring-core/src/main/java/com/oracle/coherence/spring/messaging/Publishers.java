/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.convert.ConversionService;

/**
 * {@link Publisher} related helper class.
 *
 * @author Vaso Putica
 * @since 3.0
 */
public class Publishers {
	private static final ConversionService conversionService = new ReactorConversionService();

	public static boolean isConvertibleToPublisher(Class<?> type) {
		return Publisher.class.isAssignableFrom(type)
				|| Flux.class.isAssignableFrom(type)
				|| Mono.class.isAssignableFrom(type);
	}

	public static boolean isSingle(Class<?> type) {
		return Mono.class.isAssignableFrom(type)
				|| CompletableFuturePublisher.class.isAssignableFrom(type);
	}

	public static <T> T convertPublisher(Object object, Class<T> publisherType) {
		Objects.requireNonNull(object, "Argument [object] cannot be null");
		Objects.requireNonNull(publisherType, "Argument [publisherType] cannot be null");
		if (publisherType.isInstance(object)) {
			return (T) object;
		}
		if (object instanceof CompletableFuture) {
			@SuppressWarnings("unchecked")
			Publisher<T> futurePublisher = Publishers.fromCompletableFuture(() -> ((CompletableFuture) object));
			return conversionService.convert(futurePublisher, publisherType);
		}
		else {
			return conversionService.convert(object, publisherType);
		}
	}

	public static <T> Publisher<T> fromCompletableFuture(Supplier<CompletableFuture<T>> futureSupplier) {
		return new CompletableFuturePublisher<>(futureSupplier);
	}
}
