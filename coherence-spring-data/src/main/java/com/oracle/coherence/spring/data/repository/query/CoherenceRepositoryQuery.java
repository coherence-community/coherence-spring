/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.oracle.coherence.spring.data.repository.query;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.tangosol.net.NamedMap;
import com.tangosol.util.Processors;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.DefaultParameters;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.stereotype.Repository;

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

	@SuppressWarnings("unchecked")
	@Override
	public Object execute(Object[] parameters) {

		PartTree partTree = new PartTree(this.method.getName(), this.metadata.getDomainType());
		ParameterAccessor accessor = new ParametersParameterAccessor(new DefaultParameters(this.method), parameters);
		CoherenceQueryCreator creator = new CoherenceQueryCreator(partTree, accessor);
		QueryResult q = creator.createQuery();

		if (q.getAggregator() != null) {
			return this.namedMap.aggregate(q.getFilter(), q.getAggregator());
		}
		if (partTree.isDelete()) {
			Map result = this.namedMap.invokeAll(Processors.remove(q.getFilter(), true));
			return result.size(); // TODO - more optimal way to get count?
		}

		Collection<?> result = this.namedMap.values(q.getFilter());

		if (partTree.isExistsProjection()) {
			return !result.isEmpty(); // TODO - more optimal way to determine?
		}

		return result;
	}

	@Override
	public QueryMethod getQueryMethod() {

		return this.queryMethod;
	}
}
