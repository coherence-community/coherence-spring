/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.support;

import java.lang.reflect.Method;
import java.util.Optional;

import com.oracle.coherence.spring.data.core.mapping.CoherenceMappingContext;
import com.oracle.coherence.spring.data.core.mapping.CoherencePersistentEntity;
import com.oracle.coherence.spring.data.repository.AsyncCrudRepository;
import com.oracle.coherence.spring.data.repository.BackingAsyncRepository;
import com.oracle.coherence.spring.data.repository.BackingRepository;
import com.oracle.coherence.spring.data.repository.CoherenceAsyncRepository;
import com.oracle.coherence.spring.data.repository.query.CoherenceRepositoryQuery;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.PersistentEntityInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.lang.Nullable;

/**
 * Coherence implementation of {@link RepositoryFactorySupport}.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public class CoherenceRepositoryFactory extends RepositoryFactorySupport {

	/**
	 * The Spring {@code ApplicationContext}.
	 */
	private final Coherence coherence;

	/**
	 * The {@link MappingContext}.
	 */
	private final CoherenceMappingContext mappingContext;

	/**
	 * The Coherence {@link Session} name.
	 */
	private final String sessionName;

	/**
	 * The Coherence {@link NamedMap} name.
	 */
	private final String mapName;

	/**
	 * The Coherence {@link Session}.
	 */
	private Session session;

	/**
	 * The Coherence {@link NamedMap}.
	 */
	@SuppressWarnings("rawtypes")
	private NamedMap namedMap;

	/**
	 * Creates a new {@code CoherenceRepositoryFactory}.
	 * @param coherence the {@link Coherence} instance
	 * @param mappingContext the {@link MappingContext}
	 * @param sessionName the {@link Session} name
	 * @param mapName the {@link NamedMap} name
	 */
	public CoherenceRepositoryFactory(Coherence coherence,
			CoherenceMappingContext mappingContext,
			String sessionName, String mapName) {
		this.coherence = coherence;
		this.mappingContext = mappingContext;
		this.sessionName = sessionName;
		this.mapName = mapName;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {

		CoherencePersistentEntity<?> entity = this.mappingContext.getRequiredPersistentEntity(domainClass);
		return new PersistentEntityInformation(entity);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected Object getTargetRepository(RepositoryInformation metadata) {

		Class<?> repositoryInterface = metadata.getRepositoryInterface();
		if (CoherenceAsyncRepository.class.isAssignableFrom(repositoryInterface) ||
				AsyncCrudRepository.class.isAssignableFrom(repositoryInterface)) {
			return new BackingAsyncRepository(ensureNamedMap(), this.mappingContext, metadata.getDomainType());
		}
		return new BackingRepository(ensureNamedMap(), this.mappingContext, metadata.getDomainType());
	}

	/**
	 * Ensures a {@link Session} is available.
	 * @return a {@link Session} based on the {@link #sessionName}
	 */
	private Session ensureSession() {
		if (this.session == null) {
			this.session = this.coherence.getSession(this.sessionName);
		}

		if (this.session == null) {
			throw new IllegalStateException("Unable to obtain Coherence Session." +
					"  Verify a Coherence cluster is available.");
		}
		return this.session;
	}

	/**
	 * Ensures a {@link NamedMap} is available.
	 * @return a {@link NamedMap} based on the {@link #mapName}
	 */
	@SuppressWarnings("rawtypes")
	private NamedMap ensureNamedMap() {

		if (this.namedMap == null) {
			Session session = ensureSession();
			this.namedMap = session.getMap(this.mapName);
		}
		return this.namedMap;
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {

		return BackingRepository.class;
	}

	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable QueryLookupStrategy.Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {

		return Optional.of(new CoherenceLookupStrategy(evaluationContextProvider));
	}

	/**
	 * Coherence implementation of {@link QueryLookupStrategy}.
	 */
	private final class CoherenceLookupStrategy implements QueryLookupStrategy {

		/**
		 * The {@link QueryMethodEvaluationContextProvider}.
		 */
		private final QueryMethodEvaluationContextProvider evaluationContextProvider;

		private CoherenceLookupStrategy(QueryMethodEvaluationContextProvider evalContextProvider) {
			this.evaluationContextProvider = evalContextProvider;
		}

		@Override
		public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {

			return new CoherenceRepositoryQuery(CoherenceRepositoryFactory.this.ensureNamedMap(),
					method, metadata, factory);
		}
	}
}
