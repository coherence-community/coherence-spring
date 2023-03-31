/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.oracle.coherence.spring.annotation.ChainedExtractor;
import com.oracle.coherence.spring.annotation.ExtractorFactory;
import com.oracle.coherence.spring.annotation.PofExtractor;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.tangosol.util.Extractors;
import com.tangosol.util.ValueExtractor;

/**
 * Common {@link ExtractorFactory}s for defining common Coherence {@link ValueExtractor}s.
 *
 * @author Gunnar Hillert
 * @since 3.4
 */
public class CommonExtractorFactories {

	/**
	 * A {@link ExtractorFactory} that produces {@link ValueExtractor}
	 * instances for a given property or method name.
	 * @return {@link ExtractorFactory} that produces an instance of an
	 * {@link ValueExtractor} for a given property or method name.
	 */
	@PropertyExtractor("")
	public static ExtractorFactory<PropertyExtractor, Object, Object> universalExtractor() {
		return (annotation) -> Extractors.extract(annotation.value());
	}

	/**
	 * A {@link ExtractorFactory} that produces {@link
	 * com.tangosol.util.extractor.MultiExtractor} containing {@link
	 * ValueExtractor} instances produced from the annotations contained in a
	 * {@link PropertyExtractor.Extractors} annotation.
	 * @return a {@link ExtractorFactory} that produces {@link
	 * com.tangosol.util.extractor.MultiExtractor} containing {@link
	 * ValueExtractor} instances produced from the annotations contained in a
	 * {@link PropertyExtractor.Extractors} annotation.
	 */
	@PropertyExtractor.Extractors({})
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static ExtractorFactory<PropertyExtractor.Extractors, ?, ?> universalExtractors() {
		return (annotation) -> {
			ValueExtractor[] extractors = Arrays.stream(annotation.value())
					.map((ann) -> Extractors.extract(ann.value()))
					.toArray(ValueExtractor[]::new);
			return Extractors.multi(extractors);
		};
	}

	/**
	 * A {@link ExtractorFactory} that produces chained {@link
	 * ValueExtractor} instances for an array of property or method names.
	 * @return a {@link ExtractorFactory} that produces chained {@link
	 * ValueExtractor} instances for an array of property or method names.
	 */
	@ChainedExtractor("")
	public static ExtractorFactory<ChainedExtractor, ?, ?> chainedExtractor() {
		return (annotation) -> Extractors.chained(annotation.value());
	}


	/**
	 * A {@link ExtractorFactory} that produces {@link
	 * com.tangosol.util.extractor.MultiExtractor} containing {@link
	 * ValueExtractor} instances produced from the annotations contained in a
	 * {@link ChainedExtractor.Extractors} annotation.
	 * @return a {@link ExtractorFactory} that produces {@link
	 * com.tangosol.util.extractor.MultiExtractor} containing {@link
	 * ValueExtractor} instances produced from the annotations contained in a
	 * {@link ChainedExtractor.Extractors} annotation.
	 */
	@ChainedExtractor.Extractors({})
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static ExtractorFactory<ChainedExtractor.Extractors, ?, ?> chainedExtractors() {
		return (annotation) -> {
			ValueExtractor[] extractors = Arrays.stream(annotation.value())
					.map((ann) -> Extractors.chained(ann.value()))
					.toArray(ValueExtractor[]::new);
			return Extractors.multi(extractors);
		};
	}

	/**
	 * A {@link ExtractorFactory} that produces{@link ValueExtractor}
	 * instances for a given POF index or property path.
	 * @return a {@link ExtractorFactory} that produces{@link ValueExtractor}
	 * instances for a given POF index or property path.
	 */
	@PofExtractor
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static ExtractorFactory<PofExtractor, ?, ?> pofExtractor() {
		return (annotation) -> {
			Class clazz = (annotation.type().equals(Object.class))
					? null
					: annotation.type();
			String sPath = annotation.path();
			int[] anIndex = annotation.index();

			if (sPath.length() == 0 && anIndex.length == 0) {
				throw new IllegalArgumentException("Neither 'index' nor 'path' are defined within @PofExtractor annotation. One is required.");
			}
			if (sPath.length() > 0 && anIndex.length > 0) {
				throw new IllegalArgumentException("Both 'index' and 'path' are defined within @PofExtractor annotation. Only one is allowed.");
			}
			if (sPath.length() > 0 && clazz == null) {
				throw new IllegalArgumentException("'type' must be specified within @PofExtractor annotation when property path is used.");
			}

			return (sPath.length() > 0)
					? Extractors.fromPof(clazz, sPath)
					: Extractors.fromPof(clazz, anIndex);
		};
	}

	/**
	 * A {@link ExtractorFactory} that produces {@link
	 * com.tangosol.util.extractor.MultiExtractor} containing {@link
	 * ValueExtractor} instances produced from the annotations contained in a
	 * {@link PofExtractor.Extractors} annotation.
	 * @return a {@link ExtractorFactory} that produces {@link
	 * com.tangosol.util.extractor.MultiExtractor} containing {@link
	 * ValueExtractor} instances produced from the annotations contained in a
	 * {@link PofExtractor.Extractors} annotation.
	 */
	@PofExtractor.Extractors({})
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static ExtractorFactory<PofExtractor.Extractors, ?, ?> pofExtractors() {
		final ExtractorFactory<PofExtractor, ?, ?> factory = pofExtractor();
		return (annotation) -> {
			ValueExtractor[] extractors = Arrays.stream(annotation.value())
					.map(factory::create)
					.toArray(ValueExtractor[]::new);
			return Extractors.multi(extractors);
		};
	}

	public static Map<String, ExtractorFactory> getExtractorFactories() {
		final Map<String, ExtractorFactory> extractorFactories = new HashMap<>();
		extractorFactories.put(PropertyExtractor.class.getName(), universalExtractor());
		extractorFactories.put(PropertyExtractor.Extractors.class.getName(), universalExtractors());
		extractorFactories.put(ChainedExtractor.class.getName(), chainedExtractor());
		extractorFactories.put(ChainedExtractor.Extractors.class.getName(), chainedExtractors());
		extractorFactories.put(PofExtractor.class.getName(), pofExtractor());
		extractorFactories.put(PofExtractor.Extractors.class.getName(), pofExtractors());
		return extractorFactories;
	}
}
