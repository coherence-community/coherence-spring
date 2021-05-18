/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.messaging;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Holder for Coherence Topic Listeners to be configured. See {@link CoherenceTopicListenerSubscribers}.
 *
 * @author Vaso Putica
 * @since 3.0
 *
 */
public class CoherenceTopicListenerCandidates {
	private final Map<String, List<Method>> coherenceTopicListenerCandidateMethods;

	public CoherenceTopicListenerCandidates(Map<String, List<Method>> coherenceTopicListenerCandidateMethods) {
		super();
		this.coherenceTopicListenerCandidateMethods = coherenceTopicListenerCandidateMethods;
	}

	public Map<String, List<Method>> getCoherenceTopicListenerCandidateMethods() {
		return this.coherenceTopicListenerCandidateMethods;
	}
}
