/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository.query;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.tangosol.net.NamedMap;
import com.tangosol.util.Aggregators;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.Processors;
import com.tangosol.util.filter.LimitFilter;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.DefaultParameters;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.stereotype.Repository;

import static com.oracle.coherence.spring.data.support.Utils.configureLimitFilter;
import static com.oracle.coherence.spring.data.support.Utils.toComparator;

/**
 * Coherence implementation of {@link RepositoryQuery}.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherenceRepositoryQuery implements RepositoryQuery {

	/**
	 * The {@link NamedMap} that query will be executed again.
	 */
	@SuppressWarnings("rawtypes")
	private final NamedMap namedMap;

	/**
	 * The finder method.
	 */
	private final Method method;

	/**
	 * Metadata pertaining to the associated {@link Repository}.
	 */
	private final RepositoryMetadata metadata;

	/**
	 * The {@link ProjectionFactory}.
	 */
	private final ProjectionFactory factory;

	/**
	 * The {@link QueryMethod}.
	 */
	private final QueryMethod queryMethod;

	/**
	 * Constructs a new {@code CoherenceRepositoryQuery}.
	 *
	 * @param namedMap the {@link NamedMap} this query will be executing against
	 * @param method   the finder method
	 * @param metadata the repository metadata
	 * @param factory  the {@link ProjectionFactory}
	 */
	@SuppressWarnings("rawtypes")
	public CoherenceRepositoryQuery(NamedMap namedMap, Method method, RepositoryMetadata metadata,
			ProjectionFactory factory) {

		this.namedMap = namedMap;
		this.method = method;
		this.metadata = metadata;
		this.factory = factory;
		this.queryMethod = new QueryMethod(method, metadata, factory);
	}

	@SuppressWarnings({"unchecked", "rawtypes", "ConstantConditions"})
	@Override
	public Object execute(Object[] parameters) {

		PartTree partTree = new PartTree(this.method.getName(), this.metadata.getDomainType());
		ParameterAccessor accessor = new ParametersParameterAccessor(new DefaultParameters(this.method), parameters);
		CoherenceQueryCreator creator = new CoherenceQueryCreator(partTree, accessor);
		QueryResult queryResult = creator.createQuery();

		Filter filter = queryResult.getFilter();

		if (partTree.isDelete()) {
			Map result = this.namedMap.invokeAll(Processors.remove(filter, true));
			return result.size();
		}

		Pageable pageable = accessor.getPageable();
		boolean pagingQuery = pageable != null && pageable.isPaged();
		LimitFilter limitFilter = null;
		InvocableMap.EntryAggregator aggregator = queryResult.getAggregator();

		// paging query; setup LimitFilter
		if (pagingQuery) {
			limitFilter = configureLimitFilter(pageable, filter);
			if (limitFilter != null) {
				filter = limitFilter;
			}
		}

		if (aggregator != null) {
			if (pagingQuery) {
				throw new IllegalStateException("Not possible to paginate aggregated results.");
			}
			return this.namedMap.aggregate(filter, aggregator);
		}

		Sort sort = queryResult.getSort();
		Collection<?> result = (sort.isSorted())
				? this.namedMap.values(filter, toComparator(sort))
				: this.namedMap.values(filter);

		if (partTree.isExistsProjection()) {
			return !result.isEmpty();
		}

		if (pagingQuery) {
			if (this.queryMethod.isPageQuery()) {
				int count = (int) this.namedMap.aggregate(limitFilter.getFilter(), Aggregators.count());
				return new PageImpl(Arrays.asList(result.toArray()), pageable, count);
			}
			else if (this.queryMethod.isSliceQuery()) {
				return new SliceImpl(Arrays.asList(result.toArray()), pageable, result.size() == pageable.getPageSize());
			}
		}

		return result;
	}

	@Override
	public QueryMethod getQueryMethod() {

		return this.queryMethod;
	}
}
