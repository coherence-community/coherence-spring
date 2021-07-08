/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.oracle.coherence.spring.data.support.Utils;
import com.tangosol.net.NamedMap;
import com.tangosol.util.Aggregators;
import com.tangosol.util.Extractors;
import com.tangosol.util.Filter;
import com.tangosol.util.Fragment;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.Processors;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.IdentityExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.extractor.UniversalExtractor;
import com.tangosol.util.filter.LimitFilter;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.ReturnedType;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ReflectionUtils;

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

		ResultProcessor processor = this.queryMethod.getResultProcessor().withDynamicProjection(accessor);
		ReturnedType returnedType = processor.getReturnedType();
		if (returnedType.isProjecting()) {
			return executeProjectingQuery(returnedType, queryResult, processor);
		}

		if (queryResult.getAggregator() != null) {
			return this.namedMap.aggregate(queryResult.getFilter(), queryResult.getAggregator());
		}
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

	@SuppressWarnings("unchecked")
	@Nullable
	private Object executeProjectingQuery(ReturnedType returnedType, QueryResult queryResult, ResultProcessor processor) {
		Class<?> resultType = returnedType.getReturnedType();
		List result = resultType.isInterface()
				? fetchWithInterfaceProjection(queryResult.getFilter(), resultType, returnedType.getDomainType())
				: fetchWithClassProjection(queryResult.getFilter(), resultType);

		Sort sort = queryResult.getSort();
		Comparator comparator = Utils.toComparator(sort);
		Object processedResult = processor.processResult(result);
		if (comparator != null && processedResult instanceof List) {
			((List) processedResult).sort(comparator);
		}
		return processedResult;
	}

	@SuppressWarnings("unchecked")
	private List fetchWithClassProjection(Filter filter, Class<?> resultType) {
		Constructor<?>[] constructors = resultType.getConstructors();
		if (constructors.length == 0) {
			throw new IllegalArgumentException(String.format("Type %s has no public constructor.", resultType.getName()));
		}
		Constructor constructor = constructors[0];
		String[] ctorParameterNames = Arrays.stream(constructor.getParameters())
				.map(Parameter::getName)
				.toArray(String[]::new);
		ValueExtractor[] ctorArgsExtractors = Arrays.stream(ctorParameterNames)
				.map(UniversalExtractor::new)
				.toArray(ValueExtractor[]::new);
		InvocableMap.EntryProcessor entryProcessor = Processors.extract(Extractors.fragment(ctorArgsExtractors));
		Collection<Fragment<?>> values = this.namedMap.invokeAll(filter, entryProcessor).values();
		return values.stream()
				.map((fragment) -> createDto(constructor, ctorParameterNames, fragment))
				.collect(Collectors.toList());
	}

	private Object createDto(Constructor ctor, String[] argNames, Fragment fragment) {
		Object[] args = Arrays.stream(argNames).map(fragment::get).toArray();
		try {
			return ctor.newInstance(args);
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private List fetchWithInterfaceProjection(Filter filter, Class<?> resultType, Class<?> domainType) {
		if (isOpenProjection(resultType)) {
			InvocableMap.EntryProcessor entryProcessor = Processors.extract(IdentityExtractor.INSTANCE);
			Collection<?> values = this.namedMap.invokeAll(filter, entryProcessor).values();
			return new ArrayList(values);
		}


		InvocableMap.EntryProcessor entryProcessor = Processors.extract(Extractors.fragment(createExtractors(resultType, domainType)));
		Collection<Fragment<?>> values = this.namedMap.invokeAll(filter, entryProcessor).values();
		return values.stream()
				.map(Fragment::toMap)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	private ValueExtractor[] createExtractors(Class<?> projection, Class<?> domainType) {
		List<ValueExtractor<?, ?>> extractors = new ArrayList<>();
		Method[] methods = projection.getMethods();
		for (Method m : methods) {
			Class<?> returnType = m.getReturnType();
			if (!returnType.isInterface() || isOpenProjection(returnType)) {
				extractors.add(new ReflectionExtractor<>(m.getName()));
				continue;
			}

			ValueExtractor from = new ReflectionExtractor<>(m.getName());
			Method domainMethod = ReflectionUtils.findMethod(domainType, m.getName());
			if (domainMethod == null) {
				throw new IllegalArgumentException(String.format("Method '%s' not found in ", m.getName(), domainType));
			}

			if (returnType.isAssignableFrom(domainMethod.getReturnType())) {
				extractors.add(new ReflectionExtractor<>(m.getName()));
			}
			else {
				ValueExtractor[] subExtractors = createExtractors(returnType, domainMethod.getReturnType());
				if (subExtractors.length > 0) {
					extractors.add(Extractors.fragment(from, subExtractors));
				}
			}
		}
		return extractors.toArray(new ValueExtractor[0]);
	}

	private boolean isOpenProjection(Class<?> projection) {
		return Arrays.stream(projection.getMethods())
				.anyMatch((m) -> m.isAnnotationPresent(Value.class));
	}

	@Override
	public QueryMethod getQueryMethod() {

		return this.queryMethod;
	}
}
