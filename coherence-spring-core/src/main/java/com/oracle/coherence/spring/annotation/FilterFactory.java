/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Annotation;

import com.tangosol.util.Filter;

/**
 * A factory that produces instances of {@link com.tangosol.util.Filter} for a
 * given {@link java.lang.annotation.Annotation}.
 * <p>
 * A {@link FilterFactory} is normally a CDI bean that is also annotated with a
 * {@link FilterBinding} annotation. Whenever an injection point annotated with
 * the corresponding {@link FilterBinding} annotation is encountered the {@link
 * FilterFactory} bean's {@link FilterFactory#create(java.lang.annotation.Annotation)}
 * method is called to create an instance of a {@link com.tangosol.util.Filter}.
 * @param <A> the annotation type that the factory supports
 * @param <T> the type of value being filtered
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public interface FilterFactory<A extends Annotation, T> {
	/**
	 * Create a {@link Filter} instance.
	 * @param annotation the {@link Annotation} that defines the filter
	 * @return a {@link Filter} instance
	 */
	Filter<T> create(A annotation);
}
