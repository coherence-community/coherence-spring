package com.oracle.coherence.spring.data.repository.query;

import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;

/**
 * Simple query state holder used by the query building process.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
@SuppressWarnings("rawtypes")
public class QueryState {

	/**
	 * The {@code Filter} set by a previous {@link #setFilter(Filter)} call
	 * and represents the search criteria.
	 */
	private Filter prevFilter;

	/**
	 * The most recent {@code Filter} set by {@link #setFilter(Filter)} call
	 * and represents the search criteria.
	 */
	private Filter filter;

	/**
	 * The query aggregation, if any.
	 */
	private InvocableMap.EntryAggregator aggregator;

	/**
	 * Returns the {@link Filter} that should be used by the query.
	 * @return the {@link Filter} that should be used by the query
	 */
	Filter getFilter() {
		return this.filter;
	}

	/**
	 * Set the query {@link Filter} criteria.
	 * @param f the {@link Filter} representing the current query parsing state
	 * @return this {@code QueryState}
	 */
	QueryState setFilter(Filter f) {
		if (this.filter != null) {
			this.prevFilter = this.filter;
		}
		this.filter = f;
		return this;
	}

	/**
	 * Returns the {@link InvocableMap.EntryAggregator} that should be used by the query.
	 * @return the {@link InvocableMap.EntryAggregator} that should be used by the query
	 */
	InvocableMap.EntryAggregator getAggregator() {
		return this.aggregator;
	}

	/**
	 * Set the aggregator that should be used by the query.
	 * @param aggregator the aggregator to be used by the query
	 * @return this {@code QueryState}
	 */
	QueryState setAggregator(InvocableMap.EntryAggregator aggregator) {
		this.aggregator = aggregator;
		return this;
	}

	/**
	 * {@code ANDs} the previous and current filters together making the
	 * previous filter, the current filter.
	 * @return this {@code QueryState}
	 */
	QueryState and() {
		this.filter = this.prevFilter.and(this.filter);
		this.prevFilter = null;
		return this;
	}

	/**
	 * {@code ORs} the previous and current filters together making the
	 * previous filter, the current filter.
	 * @return this {@code QueryState}
	 */
	QueryState or() {
		this.filter = this.prevFilter.or(this.filter);
		this.prevFilter = null;
		return this;
	}
}
