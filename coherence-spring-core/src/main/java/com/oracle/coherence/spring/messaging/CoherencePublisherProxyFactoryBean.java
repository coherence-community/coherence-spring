/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.oracle.coherence.spring.annotation.CoherencePublisher;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.Topic;
import com.oracle.coherence.spring.annotation.Topics;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.topic.Publisher;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Generates a proxy for the provided, {@link CoherencePublisher} annotated, interface.
 *
 * @author Vaso Putica
 * @since 3.0
 */
public class CoherencePublisherProxyFactoryBean implements FactoryBean<Object>, MethodInterceptor, BeanClassLoaderAware,
		DisposableBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private final Object initializationMonitor = new Object();

	private final Map<Method, PublisherMethod> publisherMethods = new HashMap<>();

	private final Class<?> serviceInterface;

	private Object serviceProxy;

	private volatile boolean initialized;

	private boolean proxyDefaultMethods;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private Duration maxBlock;

	public CoherencePublisherProxyFactoryBean(Class<?> serviceInterface) {
		Assert.notNull(serviceInterface, "'serviceInterface' must not be null");
		Assert.isTrue(serviceInterface.isInterface(), "'serviceInterface' must be an interface");
		this.serviceInterface = serviceInterface;
	}

	private static RuntimeException wrapException(MethodInvocation invocation, Throwable exception) {
		return new RuntimeException(
				"Exception sending message for method [" + invocation.getMethod() + "]: " + exception.getMessage(), exception
		);
	}

	public void setMaxBlock(String maxBlock) {
		if (StringUtils.hasText(maxBlock)) {
			this.maxBlock = Duration.parse(maxBlock);
		}
	}

	/**
	 * Indicate if {@code default} methods on the interface should be proxied as well.
	 * @param proxyDefaultMethods the boolean flag to proxy default methods
	 */
	public void setProxyDefaultMethods(boolean proxyDefaultMethods) {
		this.proxyDefaultMethods = proxyDefaultMethods;
	}

	// ------ MethodInterceptor ---------------------------------------------

	@Nullable
	@Override
	public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		if (AopUtils.isToStringMethod(method)) {
			return "CoherencePublisher proxy for service interface [" + this.serviceInterface + "]";
		}
		if (!this.initialized) {
			afterPropertiesSet();
		}
		PublisherMethod publisherMethod = this.publisherMethods.get(method);

		try {
			if (publisherMethod == null) {
				try {
					return invocation.proceed();
				}
				catch (Throwable throwable) {
					throw new IllegalStateException(throwable);
				}
			}
			return publisherMethod.send(invocation);
		}
		catch (Throwable ex) {
			rethrowExceptionCauseIfPossible(ex, method);
			return null; // preceding call should always throw something
		}
	}

	final void afterPropertiesSet() {
		onInit();
		this.initialized = true;
	}

	private void rethrowExceptionCauseIfPossible(Throwable originalException, Method method) throws Throwable {
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		Throwable t = originalException;
		while (t != null) {
			for (Class<?> exceptionType : exceptionTypes) {
				if (exceptionType.isAssignableFrom(t.getClass())) {
					throw t;
				}
			}
			if (t instanceof RuntimeException
					&& !(t instanceof UndeclaredThrowableException)
					&& !(t instanceof IllegalStateException && "Unexpected exception thrown".equals(t.getMessage()))) {
				throw t;
			}
			t = t.getCause();
		}
		throw originalException;
	}


	// ------ BeanClassLoaderAware ------------------------------------------

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	// ------ FactoryBean ---------------------------------------------------

	@Override
	public Object getObject() throws Exception {
		if (this.serviceProxy == null) {
			this.onInit();
			Assert.notNull(this.serviceProxy, "failed to initialize proxy");
		}
		return this.serviceProxy;
	}

	@Override
	public Class<?> getObjectType() {
		return this.serviceInterface;
	}

	protected void onInit() {
		synchronized (this.initializationMonitor) {
			if (this.initialized) {
				return;
			}

			populatePublisherMethods();

			ProxyFactory cohPublisherProxyFactory = new ProxyFactory(this.serviceInterface, this);
			cohPublisherProxyFactory.addAdvice(new DefaultMethodInvokingMethodInterceptor());
			this.serviceProxy = cohPublisherProxyFactory.getProxy(this.beanClassLoader);
			this.initialized = true;
		}
	}

	private void populatePublisherMethods() {
		Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(this.serviceInterface);
		for (Method method : methods) {
			if (Modifier.isAbstract(method.getModifiers())
					|| (method.isDefault() && this.proxyDefaultMethods)) {

				PublisherMethod publisherMethod = doCreatePublisherMethod(method);
				this.publisherMethods.put(method, publisherMethod);
			}
		}
	}

	private PublisherMethod doCreatePublisherMethod(Method method) {
		PublisherMethod publisherMethod = new PublisherMethod(method);
		publisherMethod.setMaxBlockDuration(this.maxBlock);
		publisherMethod.afterPropertiesSet();
		return publisherMethod;
	}

	// ------ DisposableBean ------------------------------------------------

	@Override
	public void destroy() throws Exception {
		this.publisherMethods.values().forEach(PublisherMethod::close);
	}

	// ----- inner class SingleSubscriber -----------------------------------

	private static final class SingleSubscriber<T> implements Subscriber<T> {
		private final CompletableFuture<T> completableFuture;
		private final MethodInvocation invocation;
		private T status;

		SingleSubscriber(CompletableFuture<T> completableFuture, MethodInvocation invocation) {
			this.completableFuture = completableFuture;
			this.invocation = invocation;
		}

		@Override
		public void onSubscribe(Subscription s) {
			s.request(1);
		}

		@Override
		public void onNext(T t) {
			this.status = t;
		}

		@Override
		public void onError(Throwable t) {
			this.completableFuture.completeExceptionally(wrapException(this.invocation, t));
		}

		@Override
		public void onComplete() {
			this.completableFuture.complete(this.status);
		}
	}

	// ----- inner class PublisherMethod ------------------------------------

	private static final class PublisherMethod implements InitializingBean, AutoCloseable  {
		protected final Log logger = LogFactory.getLog(getClass());

		private final Map<TopicKey, Publisher<Object>> publisherMap = new ConcurrentHashMap<>();
		private Class<?> returnType;
		private boolean initialized;
		private boolean isReactiveReturnType;
		private String topicName;
		private int topicArgumentIndex = -1;
		private int valueIndex = -1;
		private String sessionName;
		private Duration maxBlockDuration;

		PublisherMethod(Method method) {
			setup(method);
		}

		@Override
		public void afterPropertiesSet() {
			onInit();
			this.initialized = true;
		}

		/**
		 * Subclasses may implement this for initialization logic.
		 */
		protected void onInit() {
		}

		private void initializeIfNecessary() {
			if (!this.initialized) {
				afterPropertiesSet();
			}
		}

		protected Object send(MethodInvocation invocation) {
			initializeIfNecessary();

			Object[] arguments = invocation.getArguments();
			Object value = arguments[this.valueIndex];

			String topic = (this.topicArgumentIndex >= 0)
					? Optional.of(arguments[this.topicArgumentIndex]).map(Object::toString).orElse(null)
					: this.topicName;

			Publisher<Object> publisher = getPublisher(topic, this.sessionName);

			boolean isReactiveValue = value != null && Publishers.isConvertibleToPublisher(value.getClass());

			if (this.isReactiveReturnType) {
				// return type is a reactive type
				Flux<Publisher.Status> flux = buildSendFlux(invocation, publisher, this.maxBlockDuration, value);
				return Publishers.convertPublisher(flux, this.returnType);
			}
			else {
				if (isReactiveValue) {
					if (!Publishers.isSingle(value.getClass())) {
						CompletableFuture<List<Publisher.Status>> completableFuture = new CompletableFuture<>();
						Flux<List<Publisher.Status>> sendFlux = buildSendFlux(invocation, publisher, this.maxBlockDuration, value).collectList().flux();
						sendFlux.subscribe(new CoherencePublisherProxyFactoryBean.SingleSubscriber<>(completableFuture, invocation));
						return completableFuture;
					}
					else {
						CompletableFuture<Publisher.Status> completableFuture = new CompletableFuture<>();
						Flux<Publisher.Status> sendFlux = buildSendFlux(invocation, publisher, this.maxBlockDuration, value);
						sendFlux.subscribe(new CoherencePublisherProxyFactoryBean.SingleSubscriber<>(completableFuture, invocation));
						return completableFuture;
					}
				}
				else {
					CompletableFuture<Publisher.Status> completableFuture = new CompletableFuture<>();
					publisher.publish(value).handle((status, exception) -> {
						if (exception != null) {
							completableFuture.completeExceptionally(wrapException(invocation, exception));
						}
						else {
							completableFuture.complete(status);
						}
						return null;
					});
					return completableFuture;
				}
			}
		}

		@Nonnull
		private Publisher<Object> getPublisher(String topicName, String sessionName) {
			TopicKey key = new TopicKey(topicName, sessionName);
			return this.publisherMap.compute(key, (k, publisher) -> {
				if (publisher != null) {
					return publisher;
				}
				final Session session = Coherence.findSession(sessionName)
						.orElseThrow(() -> new IllegalStateException(String.format("No Session is configured with name '%s'.", sessionName)));
				return session.getTopic(topicName).createPublisher();
			});
		}

		private Flux<Publisher.Status> buildSendFlux(
				MethodInvocation context,
				Publisher<Object> publisher,
				Duration maxBlock,
				Object value) {

			Flux<?> valueFlux = Publishers.convertPublisher(value, Flux.class);
			Flux<Publisher.Status> sendFlux = valueFlux.flatMap((o) -> Flux.create((emitter) -> publisher.publish(o).handle((status, exception) -> {
				if (exception != null) {
					emitter.error(wrapException(context, exception));
				}
				else {
					if (status != null) {
						emitter.next(status);
					}
					emitter.complete();
				}
				return null;
			}), FluxSink.OverflowStrategy.BUFFER));

			if (maxBlock != null) {
				sendFlux.timeout(maxBlock);
			}
			return sendFlux;
		}


		private void setup(Method method) {
			this.topicName = Utils.getFirstTopicName(method).orElse(null);

			Parameter[] parameters = method.getParameters();
			for (int i = 0; i < parameters.length; i++) {
				Parameter parameter = parameters[i];
				MergedAnnotations parameterAnnotation = MergedAnnotations.from(parameter);
				if (parameterAnnotation.isPresent(Payload.class)) {
					this.valueIndex = i;
				}
				else if (parameterAnnotation.isPresent(Topics.class) || parameterAnnotation.isPresent(Topic.class)) {
					this.topicArgumentIndex = i;
				}
			}
			if (!StringUtils.hasLength(this.topicName) && this.topicArgumentIndex < 0) {
				throw new RuntimeException("No topic specified for method: " + method);
			}

			if (this.valueIndex < 0) {
				for (int i = 0; i < parameters.length; i++) {
					Parameter argument = parameters[i];
					if (argument.getAnnotation(Topic.class) == null) {
						this.valueIndex = i;
						break;
					}
				}
			}

			if (this.valueIndex < 0) {
				throw new IllegalStateException("No valid message body argument found for method: " + method.getName());
			}

			this.returnType = method.getReturnType();
			this.isReactiveReturnType = Publishers.isConvertibleToPublisher(this.returnType);
			this.sessionName = getSessionName(method).orElse(Coherence.DEFAULT_NAME);
		}

		private Optional<String> getSessionName(Method method) {
			SessionName sessionNameAnnotation = AnnotationUtils.getAnnotation(method, SessionName.class);
			if (sessionNameAnnotation != null) {
				return Optional.of(sessionNameAnnotation.value());
			}
			return Optional.empty();
		}

		void setMaxBlockDuration(Duration maxBlockDuration) {
			this.maxBlockDuration = maxBlockDuration;
		}

		@Override
		public void close() {
			for (Map.Entry<TopicKey, Publisher<Object>> entry : this.publisherMap.entrySet()) {
				Publisher<Object> publisher = entry.getValue();
				try {
					publisher.flush().get(1, TimeUnit.MINUTES);
				}
				catch (Throwable throwable) {
					this.logger.error("Error flushing publisher", throwable);
				}

				try {
					publisher.close();
				}
				catch (Throwable throwable) {
					this.logger.error("Error closing publisher", throwable);
				}
			}
		}
	}
}
