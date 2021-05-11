/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository.query;

import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;

import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

/**
 * The final result of query processing that includes the {@link Filter}
 * as the query discriminate and the {@link InvocableMap.EntryAggregator EntryAggregator}.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
@SuppressWarnings("rawtypes")
public class QueryResult {

	/**
	 * The {@link Sort} to be applied to the result.
	 */
	final Sort sort;

	/**
	 * The final query state.
	 */
	final QueryState state;

	/**
	 * Creates a new {@code QueryResult}.
	 * @param state the final state of the parsed query
	 * @param sort  the sort to be applied to the results
	 */
	public QueryResult(QueryState state, Sort sort) {

		this.state = state;
		this.sort = sort;
	}

	/**
	 * Returns the {@link Filter} criteria for the query.
	 * @return the {@link Filter} criteria for the query
	 */
	public Filter getFilter() {

		return this.state.getFilter();
	}

	/**
	 * Returns the {@link InvocableMap.EntryAggregator}, if any, for the query.
	 * @return the {@link InvocableMap.EntryAggregator}, if any, for the query
	 */
	@Nullable
	public InvocableMap.EntryAggregator getAggregator() {

		return this.state.getAggregator();
	}

	/**
	 * Return the desired {@link Sort} order.
	 * @return the desired sort order
	 */
	public Sort getSort() {

		return this.sort;
	}
}
