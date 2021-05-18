/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;
import javax.inject.Named;

import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.FilterBinding;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.SubscriberGroup;
import com.oracle.coherence.spring.configuration.ExtractorService;
import com.oracle.coherence.spring.configuration.FilterService;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.util.StringUtils;

/**
 * A bean for creating subscribers related to {@literal @}{@link com.oracle.coherence.spring.annotation.CoherenceTopicListener}.
 *
 * @author Vaso Putica
 * @since 3.0
 */
public class CoherenceTopicListenerSubscribers implements ApplicationContextAware, Coherence.LifecycleListener, AutoCloseable {
	private static final Log logger = LogFactory.getLog(CoherenceTopicListenerSubscribers.class);
	private static final Void VOID = null;
	private final FilterService filterService;
	private final ExtractorService extractorService;
	private final CoherenceTopicListenerCandidates candidates;
	private final List<TopicSubscriber<?, ?, ?>> subscribers = new ArrayList<>();
	private ApplicationContext applicationContext;
	private final Scheduler scheduler;
	private boolean subscribed;

	public CoherenceTopicListenerSubscribers(FilterService filterService,
											ExtractorService extractorService,
											CoherenceTopicListenerCandidates candidates,
											@Named("consumers") Optional<ExecutorService> executorService) {
		this.filterService = filterService;
		this.extractorService = extractorService;
		this.candidates = candidates;
		this.scheduler = executorService.map(Schedulers::fromExecutor).orElseGet(Schedulers::parallel);
	}

	public boolean isSubscribed() {
		return this.subscribed;
	}

	@Override
	public void onEvent(CoherenceLifecycleEvent event) {
		if (event.getType() == CoherenceLifecycleEvent.Type.STARTED) {
			Coherence coherence = event.getCoherence();
			createSubscribers(coherence);
		}
	}

	@PreDestroy
	@Override
	public void close() {
		this.subscribers.forEach(TopicSubscriber::close);
		this.subscribers.clear();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void createSubscribers(Coherence coherence) {
		final Map<String, List<Method>> candidates = this.candidates.getCoherenceTopicListenerCandidateMethods();

		for (Map.Entry<String, List<Method>> entry : candidates.entrySet()) {
			final String beanName = entry.getKey();
			final List<Method> methods = entry.getValue();

			for (Method method : methods) {
				final Class<?> argumentClassType = method.getParameters()[0].getType();

				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Handling Coherence %s - Bean: %s, method: %s",
							argumentClassType.getName(), beanName, method.getName()));
				}

				String topicName = Utils.getFirstTopicName(method)
						.orElse(method.getName());

				SessionName sessionNameAnn = AnnotatedElementUtils.findMergedAnnotation(method, SessionName.class);
				String sessionName = (sessionNameAnn != null)
						? sessionNameAnn.value()
						: Coherence.DEFAULT_NAME;

				if (!coherence.hasSession(sessionName)) {
					logger.info("Skipping @CoherenceTopicListener annotated method subscription " + method
							+ " Session " + sessionName + " does not exist on Coherence instance " + coherence.getName());
					continue;
				}

				Session session = coherence.getSession(sessionName);
				PublisherHolder[] sendToPublishers;
				String[] sendToTopics = getSendToTopicNames(method);
				if (sendToTopics.length > 0) {
					if (method.getReturnType().equals(Void.class)) {
						logger.info("Skipping @SendTo annotations for @CoherenceTopicListener annotated method " + method
								+ " - method return type is void");
						sendToPublishers = new PublisherHolder[0];
					}
					else {
						sendToPublishers = new PublisherHolder[sendToTopics.length];
						for (int i = 0; i < sendToTopics.length; i++) {
							NamedTopic<?> topic = session.getTopic(sendToTopics[i]);
							sendToPublishers[i] = new PublisherHolder(sendToTopics[i], topic.createPublisher());
						}
					}
				}
				else {
					sendToPublishers = new PublisherHolder[0];
				}

				List<Subscriber.Option> options = new ArrayList<>();

				MergedAnnotation<SubscriberGroup> subscriberGroupAnn = MergedAnnotations.from(method).get(SubscriberGroup.class);
				subscriberGroupAnn.getValue("value", String.class)
						.ifPresent((name) -> options.add(Subscriber.Name.of(name)));

				Set<Annotation> filterAnnotations = MergedAnnotations.from(method).stream()
						.filter((mergedAnnotation) -> mergedAnnotation.getType().isAnnotationPresent(FilterBinding.class))
						.map(MergedAnnotation::synthesize)
						.collect(Collectors.toSet());
				if (!filterAnnotations.isEmpty()) {
					Filter filter = this.filterService.resolve(filterAnnotations);
					if (filter != null) {
						options.add(Subscriber.Filtered.by(filter));
					}
				}

				Set<Annotation> extractorAnnotations = MergedAnnotations.from(method).stream()
						.filter((mergedAnnotation) -> mergedAnnotation.getType().isAnnotationPresent(ExtractorBinding.class))
						.map(MergedAnnotation::synthesize)
						.collect(Collectors.toSet());
				if (!extractorAnnotations.isEmpty()) {
					ValueExtractor extractor = this.extractorService.resolve(extractorAnnotations);
					if (extractor != null) {
						options.add(Subscriber.Convert.using(extractor));
					}
				}

				NamedTopic<?> topic = session.getTopic(topicName);
				Subscriber<?> subscriber = topic.createSubscriber(options.toArray(new Subscriber.Option[0]));
				Object bean = this.applicationContext.getBean(beanName);
				TopicSubscriber<?, ?, ?> topicSubscriber = new TopicSubscriber<>(
						topicName,
						subscriber,
						sendToPublishers,
						bean,
						method,
						this.scheduler);
				this.subscribers.add(topicSubscriber);
				topicSubscriber.nextMessage();
			}
		}
		this.subscribed = true;
	}

	static String[] getSendToTopicNames(Method method) {
		return Stream.concat(
				AnnotatedElementUtils.getAllMergedAnnotations(method.getDeclaringClass(), SendTo.class).stream(),
				AnnotatedElementUtils.getAllMergedAnnotations(method, SendTo.class).stream())
				.flatMap((ann) -> Arrays.stream(ann.value()))
				.filter(StringUtils::hasLength)
				.toArray(String[]::new);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings({"unchecked"})
	static class PublisherHolder implements AutoCloseable {
		private final String topicName;
		private final Publisher publisher;

		PublisherHolder(String topicName, Publisher<?> publisher) {
			this.topicName = topicName;
			this.publisher = publisher;
		}

		String getTopicName() {
			return this.topicName;
		}

		Publisher<?> getPublisher() {
			return this.publisher;
		}

		@Override
		public void close() {
			this.publisher.close();
		}

		CompletableFuture<Publisher.Status> send(Object message) {
			return this.publisher.publish(message);
		}
	}

	/**
	 * A topic subscriber that wraps an {@link Method}.
	 *
	 * @param <E> the type of the topic elements
	 * @param <T> the type of the bean declaring the {@link Method}
	 * @param <R> the method return type of the {@link Method}
	 */
	static class TopicSubscriber<E, T, R> implements AutoCloseable {
		/**
		 * The name of the subscribed topic.
		 */
		private final String topicName;

		/**
		 * The actual topic {@link com.tangosol.net.topic.Subscriber}.
		 */
		private final Subscriber<E> subscriber;

		/**
		 * The optional topic {@link com.tangosol.net.topic.Publisher Publishers} to send
		 * any method return value to.
		 */
		private final PublisherHolder[] publishers;

		/**
		 * The bean declaring the {@link Method}.
		 */
		private final T bean;

		/**
		 * The {@link Method} to forward topic elements to.
		 */
		private final Method method;

		/**
		 * The scheduler service.
		 */
		private final Scheduler scheduler;

		/**
		 * Create a {@link TopicSubscriber}.
		 *
		 * @param topicName        the name of the subscribed topic.
		 * @param subscriber       the actual topic {@link com.tangosol.net.topic.Subscriber}
		 * @param publishers       the optional {@link Publisher Publishers} to send any method return type to
		 * @param bean             the bean declaring the {@link Method}
		 * @param method           the {@link Method} to forward topic elements to
		 * @param scheduler        the scheduler service
		 */
		TopicSubscriber(String topicName, Subscriber<E> subscriber, PublisherHolder[] publishers, T bean,
						Method method, Scheduler scheduler) {
			this.topicName = topicName;
			this.subscriber = subscriber;
			this.publishers = publishers;
			this.bean = bean;
			this.method = method;
			this.scheduler = scheduler;
		}

		@Override
		public void close() {
			try {
				this.subscriber.close();
			}
			catch (Throwable throwable) {
				logger.error("Error closing subscriber for topic " + this.topicName, throwable);
			}
		}

		/**
		 * <p>Request the next message from the {@link com.tangosol.net.topic.Subscriber}.</p>
		 * <p>If requesting the next message throws an exception the subscription will
		 * end and the {@link com.tangosol.net.topic.Subscriber} will be closed.</p>
		 */
		private void nextMessage() {
			this.subscriber.receive().handle(this::handleMessage)
					.handle((v, err) -> {
						if (err != null) {
							logger.error("Error requesting message from topic " + this.topicName
									+ " for method " + this.method + " - subscriber will be closed", err);
							this.subscriber.close();
						}
						return VOID;
					});
		}

		/**
		 * <p>After the {@link Method} handles the message
		 * the next message will be requested from the subscriber.</p>
		 * <p>If the response is an error the subscription will end and the
		 * {@link com.tangosol.net.topic.Subscriber} will be closed.</p>
		 * <p>If the call to the {@link Method} throws
		 * an exception the subscription will end and the {@link com.tangosol.net.topic.Subscriber}
		 * will be closed.</p>
		 * @param element    the {@link com.tangosol.net.topic.Subscriber.Element} received
		 * @param throwable  any error from the subscriber
		 * @return always returns {@link java.lang.Void} (i.e. {@code null})
		 */
		private Void handleMessage(Subscriber.Element<E> element, Throwable throwable) {
			if (throwable != null) {
				if (!(throwable instanceof CancellationException)) {
					logger.error("Error receiving message from topic " + this.topicName
							+ " for method " + this.method + " - subscriber will be closed", throwable);
					this.subscriber.close();
				}
				return VOID;
			}

			try {
				E value = element.getValue();
				Object result = this.method.invoke(this.bean, value);
				handleResult(result);
			}
			catch (Throwable ex) {
				logger.error("Error processing message from topic " + this.topicName, ex);
			}
			nextMessage();
			return VOID;
		}

		/**
		 * Handle the listener method result and if required forward to publishers.
		 * @param result the method result
		 */
		private void handleResult(Object result) {
			if (result == null || this.publishers.length == 0) {
				return;
			}

			if (result.getClass().isArray()) {
				result = Arrays.asList((Object[]) result);
			}

			Class<?> type = result.getClass();
			boolean isAsyncReturnType = CompletionStage.class.isAssignableFrom(type);

			if (isAsyncReturnType) {
				((CompletionStage<?>) result)
						.handle((msg, err1) -> {
							if (err1 == null) {
								handleResult(msg);
							}
							else {
								logger.error("Method " + this.method + " async result completed with an error", err1);
							}
							return VOID;
						});
			}
			else {
				Flux<?> resultFlux = (Publishers.isConvertibleToPublisher(result.getClass()))
						? Publishers.convertPublisher(result, Flux.class)
						: Flux.just(result);
				handleResultFlux(this.method, resultFlux);
			}
		}

		/**
		 * Handle a listener method result that is a reactive object.
		 * @param method          the listener method
		 * @param resultFlux      the flux result
		 */
		private void handleResultFlux(Method method, Flux<?> resultFlux) {
			Flux<?> recordMetadataProducer = resultFlux.subscribeOn(this.scheduler)
					.flatMap((Function<Object, org.reactivestreams.Publisher<?>>) (o) -> {
						if (this.publishers.length > 0) {
							return Flux.create((emitter) -> {
								for (PublisherHolder publisher : this.publishers) {
									publisher.send(o).handle((ignored, exception) -> {
										if (exception != null) {
											emitter.error(exception);
										}
										return VOID;
									});
								}
								emitter.complete();
							}, FluxSink.OverflowStrategy.ERROR);
						}
						return Flux.empty();
					}).onErrorResume((throwable) -> {
						logger.error("Error processing result from method " + method, throwable);
						return Flux.empty();
					});

			recordMetadataProducer.subscribe((recordMetadata) -> {
				if (logger.isTraceEnabled()) {
					logger.trace(String.format("Method [%s] produced record metadata: %s", method, recordMetadata));
				}
			});
		}
	}
}
