/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link ExtractorBinding} annotation representing a {@link com.tangosol.util.extractor.ChainedExtractor}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Inherited
@ExtractorBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ChainedExtractor.Extractors.class)
public @interface ChainedExtractor {
	/**
	 * Returns the a method or property name to use when creating a {@link
	 * com.tangosol.util.extractor.ChainedExtractor}.
	 * <p>
	 * If the value does not end in {@code "()"} the value is assumed to be a
	 * property name. If the value is prefixed with one of the accessor prefixes
	 * {@code "get"} or {@code "is"} and ends in {@code "()"} this extractor is
	 * a property extractor. Otherwise, if the value just ends in {@code "()"}
	 * this value is considered a method name.
	 *
	 * @return the value used for the where clause when creating a {@link
	 * com.tangosol.util.extractor.ChainedExtractor}
	 */
	String[] value();

	/**
	 * A holder for the repeatable {@link ChainedExtractor} annotation.
	 */
	@Inherited
	@ExtractorBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@interface Extractors {

		// Dummy field - without it, the annotation does not work with Spring
		String can_be_anything() default "";

		/**
		 * An array of {@link ChainedExtractor}s.
		 *
		 * @return an array of {@link ChainedExtractor}s
		 */
		ChainedExtractor[] value();

	}
}
