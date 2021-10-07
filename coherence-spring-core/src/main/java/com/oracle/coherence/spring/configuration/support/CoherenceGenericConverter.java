/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tangosol.net.NamedCache;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

/**
 * This implementation of Spring's {@link GenericConverter} is a no-op converter for Coherence's {@link NamedCache}. Its
 * purpose is to work around an unnecessary type-conversion that may be triggered by the
 * {@link com.oracle.coherence.spring.configuration.annotation.CoherenceMap} and
 * {@link com.oracle.coherence.spring.cache.CoherenceCache} annotation (Due to the used
 * {@link org.springframework.beans.factory.annotation.Value} annotation).
 *
 * @author Gunnar Hillert
 * @since 3.0.1
 * @see com.oracle.coherence.spring.configuration.CoherenceConversionServicePostProcessor
 */
public class CoherenceGenericConverter implements GenericConverter {

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		final ConvertiblePair[] pairs = new ConvertiblePair[] {
				new ConvertiblePair(NamedCache.class, Map.class),
		};
		return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(pairs)));
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			return source;
	}

}
