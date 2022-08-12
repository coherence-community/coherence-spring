/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration;

import java.util.Arrays;

import jakarta.inject.Inject;

import com.oracle.coherence.spring.annotation.ChainedExtractor;
import com.oracle.coherence.spring.annotation.ExtractorFactory;
import com.oracle.coherence.spring.annotation.PofExtractor;
import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.tangosol.util.Extractors;
import com.tangosol.util.ValueExtractor;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

/**
 * Configures the beans to support {@link com.tangosol.util.ValueExtractor}s.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Configuration
public class ExtractorConfiguration {

	private final ExtractorService extractorService;

	/**
	 * Create a {@link ExtractorConfiguration} that will use the specified bean context.
	 * @param extractorService the {@link ExtractorService} to use
	 */
	@Inject
	ExtractorConfiguration(ExtractorService extractorService) {
		this.extractorService = extractorService;
	}

	@Bean
	@Primary
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public ValueExtractor<?, ?> getExtractor(InjectionPoint injectionPoint) {
		return this.extractorService.getExtractor(injectionPoint);
	}

	/**
	 * A {@link ExtractorFactory} that produces {@link ValueExtractor}
	 * instances for a given property or method name.
	 * @return {@link ExtractorFactory} that produces an instance of an
	 * {@link ValueExtractor} for a given property or method name.
	 */
	@Bean
	@PropertyExtractor("")
	ExtractorFactory<PropertyExtractor, Object, Object> universalExtractor() {
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
	@Bean
	@PropertyExtractor.Extractors({})
	@SuppressWarnings({"unchecked", "rawtypes"})
	ExtractorFactory<PropertyExtractor.Extractors, ?, ?> universalExtractors() {
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
	@Bean
	@ChainedExtractor("")
	ExtractorFactory<ChainedExtractor, ?, ?> chainedExtractor() {
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
	@Bean
	@ChainedExtractor.Extractors({})
	@SuppressWarnings({"unchecked", "rawtypes"})
	ExtractorFactory<ChainedExtractor.Extractors, ?, ?> chainedExtractors() {
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
	@Bean
	@PofExtractor
	@SuppressWarnings({"unchecked", "rawtypes"})
	ExtractorFactory<PofExtractor, ?, ?> pofExtractor() {
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
	@Bean
	@PofExtractor.Extractors({})
	@SuppressWarnings({"unchecked", "rawtypes"})
	ExtractorFactory<PofExtractor.Extractors, ?, ?> pofExtractors() {
		final ExtractorFactory<PofExtractor, ?, ?> factory = pofExtractor();
		return (annotation) -> {
			ValueExtractor[] extractors = Arrays.stream(annotation.value())
					.map(factory::create)
					.toArray(ValueExtractor[]::new);
			return Extractors.multi(extractors);
		};
	}
}
