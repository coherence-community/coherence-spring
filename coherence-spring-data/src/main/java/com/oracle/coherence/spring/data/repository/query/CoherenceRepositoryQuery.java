/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository.query;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.tangosol.net.NamedMap;
import com.tangosol.util.Processors;
import com.tangosol.util.extractor.UniversalExtractor;
import com.tangosol.util.function.Remote;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
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

import static com.tangosol.util.function.Remote.comparator;

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

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object execute(Object[] parameters) {

		Class<?> returnType = this.method.getReturnType();
		if (returnType == Page.class || returnType == Slice.class) {
			throw new UnsupportedOperationException("The M1 release of spring-coherence-data doesn't" +
					" support Slice or Page return types.");
		}

		PartTree partTree = new PartTree(this.method.getName(), this.metadata.getDomainType());
		ParameterAccessor accessor = new ParametersParameterAccessor(new DefaultParameters(this.method), parameters);
		CoherenceQueryCreator creator = new CoherenceQueryCreator(partTree, accessor);
		QueryResult q = creator.createQuery();

		if (q.getAggregator() != null) {
			return this.namedMap.aggregate(q.getFilter(), q.getAggregator());
		}
		if (partTree.isDelete()) {
			Map result = this.namedMap.invokeAll(Processors.remove(q.getFilter(), true));
			return result.size();
		}

		Sort sort = q.getSort();
		Remote.Comparator comparator = null;
		if (sort.isSorted()) {
			for (Sort.Order order : sort) {
				if (comparator == null) {
					comparator = comparator(new UniversalExtractor(order.getProperty()));
					if (order.isDescending()) {
						comparator = comparator.reversed();
					}
				}
				else {
					Remote.Comparator temp = comparator(new UniversalExtractor(order.getProperty()));
					if (order.isDescending()) {
						temp = comparator.reversed();
					}
					comparator.thenComparing(temp);
				}
			}
		}
		Collection<?> result = (comparator != null)
				? this.namedMap.values(q.getFilter(), comparator)
				: this.namedMap.values(q.getFilter());

		if (partTree.isExistsProjection()) {
			return !result.isEmpty();
		}

		return result;
	}

	@Override
	public QueryMethod getQueryMethod() {

		return this.queryMethod;
	}
}
