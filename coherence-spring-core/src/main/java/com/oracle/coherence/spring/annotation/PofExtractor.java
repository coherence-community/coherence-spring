/*
 * Copyright (c) 2013, 2023, Oracle and/or its affiliates.
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

import com.tangosol.io.pof.generator.PortableTypeGenerator;
import com.tangosol.io.pof.schema.annotation.PortableType;

/**
 * A {@link ExtractorBinding} annotation representing a {@link
 * com.tangosol.util.extractor.PofExtractor}.
 * <p>
 * This annotation can be used to define an extractor that extracts and attribute
 * from a POF stream based on an array of integer property indices, in which
 * case the type is optional, or a property path based on serialized field names
 * concatenated using period (ie. {@code address.city}, in which case {@link
 * #type()} attribute must be set as well.
 * <p>
 * The latter approach can only be used if the specified type is annotated with a
 * {@link PortableType @PortableType} annotation and has been instrumented using
 * {@link PortableTypeGenerator} (typically via {@code pof-maven-plugin}).
 * <p>
 * Either {@link #index()} or {@link #path()} must be specified within this
 * annotation in order for it to be valid.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@Inherited
@ExtractorBinding
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PofExtractor.Extractors.class)
public @interface PofExtractor {

	/**
	 * Returns an array of POF indexes to use to extract the value.
	 * @return an array of POF indexes to use to extract the value
	 */
	int[] index() default {};

	/**
	 * Returns a property path to use to extract the value.
	 * <p>
	 * This attribute can only be used in combination with the {@link #type()}
	 * attribute, and only if the specified type is annotated with a
	 * {@link PortableType @PortableType} annotation and instrumented using
	 * {@link PortableTypeGenerator}.
	 * @return a property path to use to extract the value
	 */
	String path() default "";

	/**
	 * Returns the root type to extract property from.
	 * @return the root type to extract property from
	 */
	Class<?> type() default Object.class;

	/**
	 * A holder for the repeatable {@link PofExtractor} annotation.
	 */
	@Inherited
	@ExtractorBinding
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@interface Extractors {
		PofExtractor[] value();
	}
}
