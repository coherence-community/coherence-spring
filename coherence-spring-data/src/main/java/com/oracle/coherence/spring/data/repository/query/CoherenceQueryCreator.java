/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;

import com.tangosol.util.Aggregators;
import com.tangosol.util.Extractors;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;

/**
 * Coherence implementation of {@link AbstractQueryCreator}.
 *
 * @author Ryan Lubke
 * @since 3.0
 */
@SuppressWarnings("rawtypes")
public class CoherenceQueryCreator extends AbstractQueryCreator<QueryResult, QueryState> {

	/**
	 * LIKE filter wildcard.
	 */
	private static final char WILDCARD = '%';

	/**
	 * The {@link QueryState} that will be used as the framework progresses
	 * through a query expression.
	 */
	private final QueryState criteria = new QueryState();

	/**
	 * The parsed steps of a finder query.
	 */
	private final PartTree partTree;

	/**
	 * Construct a new {@code CoherenceQueryCreator} that will create a Coherence-specific
	 * {@link QueryResult} containing the necessary components to execute a query against
	 * a Coherence grid.
	 * @param tree       the {@link PartTree} generated from a finder expression
	 * @param parameters the parameters to pass to the query creation process
	 */
	public CoherenceQueryCreator(PartTree tree, ParameterAccessor parameters) {

		super(tree, parameters);
		this.partTree = tree;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected QueryState create(Part part, Iterator<Object> iterator) {

		Objects.requireNonNull(part, "part must not be null");
		Objects.requireNonNull(iterator, "iterator must not be null");

		String prop = part.getProperty().toDotPath();
		boolean ignoreCase = isIgnoreCase(part);

		switch (part.getType()) {
			case SIMPLE_PROPERTY:
				return setFilter(ignoreCase
						? Filters.like(Extractors.extract(prop), iterator.next().toString(), true)
						: Filters.equal(prop, iterator.next()));
			case BETWEEN:
				return setFilter(Filters.between(Extractors.extract(prop),
						(Comparable) iterator.next(),    // lower bound
						(Comparable) iterator.next()));  // upper bound
			case IS_NOT_NULL:
			case EXISTS:
				return setFilter(Filters.isNotNull(Extractors.extract(prop)));
			case IS_NULL:
				return setFilter(Filters.isNull(Extractors.extract(prop)));
			case LESS_THAN:
			case BEFORE:
				return setFilter(Filters.less(Extractors.extract(prop),
						(Comparable) iterator.next()));
			case LESS_THAN_EQUAL:
				return setFilter(Filters.lessEqual(Extractors.extract(prop),
						(Comparable) iterator.next()));
			case GREATER_THAN:
			case AFTER:
				return setFilter(Filters.greater(Extractors.extract(prop),
						(Comparable) iterator.next()));
			case GREATER_THAN_EQUAL:
				return setFilter(Filters.greaterEqual(Extractors.extract(prop),
						(Comparable) iterator.next()));
			case NOT_LIKE:
				return setFilter(Filters.not(Filters.like(Extractors.extract(prop),
						iterator.next().toString(), ignoreCase)));
			case LIKE:
				return setFilter(Filters.like(Extractors.extract(prop),
						iterator.next().toString(), ignoreCase));
			case STARTING_WITH:
				return setFilter(Filters.like(Extractors.extract(prop),
						iterator.next().toString() + WILDCARD, ignoreCase));
			case ENDING_WITH:
				return setFilter(Filters.like(Extractors.extract(prop),
						WILDCARD + iterator.next().toString(), ignoreCase));
			case IS_NOT_EMPTY:
				return setFilter(Filters.isFalse(Extractors.chained(prop, "isEmpty()")));
			case IS_EMPTY:
				return setFilter(Filters.isTrue(Extractors.chained(prop, "isEmpty()")));
			case NOT_CONTAINING:
				return setFilter(Filters.not(Filters.like(Extractors.extract(prop),
						WILDCARD + iterator.next().toString() + WILDCARD)));
			case CONTAINING:
				return setFilter(Filters.like(Extractors.extract(prop),
						WILDCARD + iterator.next().toString() + WILDCARD));
			case NOT_IN:
				return setFilter(Filters.not(Filters.in(Extractors.extract(prop),
						new HashSet((Collection) iterator.next()))));
			case IN:
				return setFilter(Filters.in(Extractors.extract(prop),
						new HashSet((Collection) iterator.next())));
			case NEAR:
			case WITHIN:
				throw new UnsupportedOperationException("Unsupported keyword: "
						+ part.getType());
			case REGEX:
				return setFilter(Filters.regex(Extractors.extract(prop),
						iterator.next().toString()));
			case TRUE:
				return setFilter(Filters.isTrue(Extractors.extract(prop)));
			case FALSE:
				return setFilter(Filters.isFalse(Extractors.extract(prop)));
			case NEGATING_SIMPLE_PROPERTY:
				return setFilter(Filters.not(
						ignoreCase
							? Filters.like(Extractors.extract(prop), iterator.next().toString(), true)
							: Filters.equal(prop, iterator.next())));
		}
		return this.criteria;
	}

	private boolean isIgnoreCase(Part part) {
		switch (part.shouldIgnoreCase()) {
			case ALWAYS:
			case WHEN_POSSIBLE:
				return true;
			default:
				return false;
		}
	}

	private QueryState setFilter(Filter filter) {

		Objects.requireNonNull(filter, "filter must not be null");
		return this.criteria.setFilter(filter);
	}

	/**
	 * {@inheritDoc}
	 * @param part {@inheritDoc}
	 * @param base ignored - we use the same {@link QueryState} instance during the lifetime
	 *             of the query processing
	 * @param iterator {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	protected QueryState and(Part part, @Nullable QueryState base,
			Iterator<Object> iterator) {

		create(part, iterator);
		return this.criteria.and();
	}

	/**
	 * {@inheritDoc}
	 * @param base ignored - we use the same {@link QueryState} instance during the lifetime
	 *             of the query processing
	 * @param criteria {@inheritDoc}
	 * @return {@inheritDoc}
	 */
	@Override
	protected QueryState or(QueryState base, QueryState criteria) {
		return this.criteria.or();
	}

	@SuppressWarnings("NullableProblems") // there will always be a QueryState
	@Override
	protected QueryResult complete(QueryState criteria, Sort sort) {
		if (this.partTree.hasPredicate()) {
			if (this.partTree.isCountProjection()) {
				this.criteria.setAggregator(Aggregators.count());
			}
			else if (this.partTree.isDistinct()) {
				this.criteria.setAggregator(Aggregators.distinctValues());
			}
			else if (this.partTree.isLimiting()) {
				Integer limit = this.partTree.getMaxResults();
				assert limit != null;
				criteria.setFilter(criteria.getFilter().asLimitFilter(limit));
			}
		}
		return new QueryResult(criteria, sort);
	}
}
