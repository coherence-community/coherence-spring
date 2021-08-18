/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure.session;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Custom Coherence-specific condition for Session Configuration. This {@link SpringBootCondition}
 * will not match if property {@code spring.session.store-type} is set.
 *
 * see also: https://github.com/spring-projects/spring-boot/issues/27738
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
class CoherenceSpringSessionCondition extends SpringBootCondition {

	private static final String STORE_TYPE_PROPERTY = "spring.session.store-type";

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		final ConditionMessage.Builder message = ConditionMessage.forCondition("Coherence Session Condition");
		final Environment environment = context.getEnvironment();

		if (!environment.containsProperty(STORE_TYPE_PROPERTY)) {
			return ConditionOutcome.match(message.didNotFind("property", "properties")
					.items(ConditionMessage.Style.QUOTE, STORE_TYPE_PROPERTY));
		}
		else {
			return ConditionOutcome.noMatch(message.found(
					String.format("%s property (value: '%s')",
							STORE_TYPE_PROPERTY, environment.getProperty(STORE_TYPE_PROPERTY))).atAll());
		}
	}
}
