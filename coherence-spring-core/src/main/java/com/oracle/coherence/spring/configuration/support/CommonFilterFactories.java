/*
 * Copyright (c) 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.configuration.support;

import java.util.HashMap;
import java.util.Map;

import com.oracle.coherence.spring.annotation.AlwaysFilter;
import com.oracle.coherence.spring.annotation.FilterFactory;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.QueryHelper;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Common {@link FilterFactory}s for defining Coherence {@link Filter}s.
 *
 * @author Gunnar Hillert
 * @since 3.4
 */
public class CommonFilterFactories {

	/**
	 * Produce a {@link FilterFactory} that produces an instance of an
	 * {@link com.tangosol.util.filter.AlwaysFilter}.
	 * @return a {@link FilterFactory} that produces an instance of an
	 *         {@link com.tangosol.util.filter.AlwaysFilter}
	 */
	@AlwaysFilter
	public static FilterFactory<AlwaysFilter, ?> alwaysFactory() {
		return (annotation) -> Filters.always();
	}

	/**
	 * Produce a {@link FilterFactory} that produces an instance of a
	 * {@link com.tangosol.util.Filter} created from a CohQL where clause.
	 * @return a {@link FilterFactory} that produces an instance of an
	 *         {@link com.tangosol.util.Filter} created from a CohQL
	 *         where clause
	 */
	@WhereFilter("")
	@SuppressWarnings("unchecked")
	@Qualifier
	public static FilterFactory<WhereFilter, ?> whereFactory() {
		return (annotation) -> {
			String sWhere = annotation.value();
			return sWhere.trim().isEmpty() ? Filters.always() : QueryHelper.createFilter(annotation.value());
		};
	}

	public static Map<String, FilterFactory> getFilterFactories() {
		final Map<String, FilterFactory> filterFactories = new HashMap<>();
		filterFactories.put(AlwaysFilter.class.getName(), alwaysFactory());
		filterFactories.put(WhereFilter.class.getName(), whereFactory());
		return filterFactories;
	}
}
