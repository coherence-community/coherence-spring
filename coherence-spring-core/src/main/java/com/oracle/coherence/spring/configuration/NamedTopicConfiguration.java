/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.oracle.coherence.spring.annotation.ExtractorBinding;
import com.oracle.coherence.spring.annotation.FilterBinding;
import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.SubscriberGroup;
import com.tangosol.net.Coherence;
import com.tangosol.net.Session;
import com.tangosol.net.topic.NamedTopic;
import com.tangosol.net.topic.Publisher;
import com.tangosol.net.topic.Subscriber;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.StringUtils;

/**
 * Provides support for injecting Coherence Topics using {@link NamedTopic}.
 *
 * @author Vaso Putica
 * @since 3.0
 */
@Configuration
public class NamedTopicConfiguration {
	protected static final Log logger = LogFactory.getLog(NamedTopicConfiguration.class);

	final FilterService filterService;
	final ExtractorService extractorService;

	public NamedTopicConfiguration(FilterService filterService, ExtractorService extractorService) {
		this.filterService = filterService;
		this.extractorService = extractorService;
	}

	@Bean(destroyMethod = "release")
	@DependsOn(CoherenceSpringConfiguration.COHERENCE_SERVER_BEAN_NAME)
	@Primary
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<V> NamedTopic<V> getTopic(InjectionPoint injectionPoint) {
		return getTopicInternal(injectionPoint);
	}

	@Bean(destroyMethod = "release")
	@DependsOn(CoherenceSpringConfiguration.COHERENCE_SERVER_BEAN_NAME)
	@Primary
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<V> Publisher<V> getPublisher(InjectionPoint injectionPoint) {
		NamedTopic<V> topic = getTopicInternal(injectionPoint);
		return topic.createPublisher();
	}

	@SuppressWarnings("unchecked")
	@Bean(destroyMethod = "release")
	@DependsOn(CoherenceSpringConfiguration.COHERENCE_SERVER_BEAN_NAME)
	@Primary
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	<V> Subscriber<V> getSubscriber(InjectionPoint injectionPoint) {
		List<Subscriber.Option> options = new ArrayList<>();

		final MergedAnnotations mergedAnnotations = MergedAnnotations.from(injectionPoint.getAnnotatedElement());

		final MergedAnnotation<SubscriberGroup> mergedSubscribedGroupAnnotation = mergedAnnotations.get(SubscriberGroup.class);

		if (mergedSubscribedGroupAnnotation.isPresent()) {
			String subscribedGroupName = mergedSubscribedGroupAnnotation.synthesize().value();
			if (StringUtils.hasLength(subscribedGroupName)) {
				options.add(Subscriber.Name.of(subscribedGroupName));
			}
		}

		final MergedAnnotation<FilterBinding> mergedFilterBindingAnnotation = mergedAnnotations.get(FilterBinding.class);
		if (mergedFilterBindingAnnotation.isPresent()) {
			Filter filter = this.filterService.getFilter(injectionPoint);
			options.add(Subscriber.Filtered.by(filter));
		}

		final MergedAnnotation<ExtractorBinding> mergedExtractorBindingAnnotation = mergedAnnotations.get(ExtractorBinding.class);
		if (mergedExtractorBindingAnnotation.isPresent()) {
			ValueExtractor extractor = this.extractorService.getExtractor(injectionPoint);
			options.add(Subscriber.Convert.using(extractor));
		}

		NamedTopic<V> topic = getTopicInternal(injectionPoint);
		return options.isEmpty()
				? topic.createSubscriber()
				: topic.createSubscriber(options.toArray(new Subscriber.Option[0]));
	}

	private <V> NamedTopic<V> getTopicInternal(InjectionPoint injectionPoint) {
		final MergedAnnotations mergedAnnotations = MergedAnnotations.from(injectionPoint.getAnnotatedElement());

		final MergedAnnotation<SessionName> mergedSessionNameAnnotation = mergedAnnotations.get(SessionName.class);
		final MergedAnnotation<Name> mergedNameAnnotation = mergedAnnotations.get(Name.class);

		final String sessionName;
		final String topicName = this.determineTopicName(injectionPoint, mergedNameAnnotation);

		if (!mergedSessionNameAnnotation.isPresent() || mergedSessionNameAnnotation.synthesize().value().trim().isEmpty()) {
			sessionName = Coherence.DEFAULT_NAME;
		}
		else {
			sessionName = mergedSessionNameAnnotation.synthesize().value();
		}

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Going to retrieve NamedTopic '%s' for session '%s'.", topicName, sessionName));
		}

		if (topicName == null || topicName.trim().isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot determine topic name. No @Name qualifier and injection point is not named");
		}

		final Session session = Coherence.findSession(sessionName)
				.orElseThrow(() -> new IllegalStateException(String.format("No Session is configured with name '%s'.", sessionName)));

		return session.getTopic(topicName);
	}

	private String determineTopicName(InjectionPoint injectionPoint, MergedAnnotation<Name> mergedNameAnnotation) {
		final String topicName;

		if (!mergedNameAnnotation.isPresent() || !StringUtils.hasText(mergedNameAnnotation.synthesize().value())) {

			final Field field = injectionPoint.getField();

			if (field == null) {
				final MethodParameter methodParameter = injectionPoint.getMethodParameter();
				if (methodParameter != null) {
					final String parameterName = methodParameter.getParameterName();
					if (parameterName != null) {
						topicName = parameterName;
					}
					else {
						throw new IllegalStateException("Unable to retrieve the name of the method parameter");
					}
				}
				else {
					throw new IllegalStateException("Not an annotated field nor a method parameter");
				}
			}
			else {
				topicName = field.getName();
			}
		}
		else {
			topicName = mergedNameAnnotation.synthesize().value();
		}
		return topicName;
	}
}
