/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 */
package com.oracle.coherence.spring.messaging;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Adapts a {@link CompletableFuture} to a {@link org.reactivestreams.Publisher}.
 *
 * @param <T> the type
 * @author Graeme Rocher
 * @since 3.0
 */
public class CompletableFuturePublisher<T> implements Publisher<T> {
	private final Supplier<CompletableFuture<T>> futureSupplier;

	/**
	 * Completable Future Publisher constructor.
	 *
	 * @param futureSupplier the function that supplies the future.
	 */
	CompletableFuturePublisher(Supplier<CompletableFuture<T>> futureSupplier) {
		this.futureSupplier = futureSupplier;
	}

	@Override
	public final void subscribe(Subscriber<? super T> subscriber) {
		Objects.requireNonNull(subscriber, "Subscriber cannot be null");
		subscriber.onSubscribe(new CompletableFutureSubscription(subscriber));
	}

	/**
	 * CompletableFuture subscription.
	 */
	class CompletableFutureSubscription implements Subscription {
		private final Subscriber<? super T> subscriber;
		private final AtomicBoolean completed = new AtomicBoolean(false);
		private CompletableFuture<T> future; // to allow cancellation

		/**
		 * CompletableFutureSubscriber constructor.
		 * @param subscriber the subscriber
		 */
		CompletableFutureSubscription(Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
		}

		@Override
		public synchronized void request(long n) {
			if (n == 0 || this.completed.get()) {
				return;
			}
			if (n < 0) {
				IllegalArgumentException ex = new IllegalArgumentException("Cannot request a negative number");
				this.subscriber.onError(ex);
				return;
			}
			try {
				CompletableFuture<T> future = CompletableFuturePublisher.this.futureSupplier.get();
				if (future == null) {
					this.subscriber.onComplete();
				}
				else {
					this.future = future;
					future.whenComplete((s, throwable) -> {
						if (this.completed.compareAndSet(false, true)) {
							if (throwable != null) {
								this.subscriber.onError(throwable);
							}
							else {
								if (s != null) {
									this.subscriber.onNext(s);
								}
								this.subscriber.onComplete();
							}
						}
					});
				}
			}
			catch (Throwable ex) {
				this.subscriber.onError(ex);
			}
		}

		/**
		 * Request the publisher to stop sending data and clean up resources.
		 */
		@Override
		public synchronized void cancel() {
			if (this.completed.compareAndSet(false, true) && this.future != null) {
				this.future.cancel(false);
			}
		}
	}
}
