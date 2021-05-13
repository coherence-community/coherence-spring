/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.oracle.coherence.repository.AbstractAsyncRepository;
import com.oracle.coherence.spring.data.core.mapping.CoherencePersistentEntity;
import com.oracle.coherence.spring.data.core.mapping.CoherencePersistentProperty;
import com.tangosol.net.AsyncNamedMap;
import com.tangosol.net.NamedMap;
import com.tangosol.util.Base;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.filter.InKeySetFilter;

import org.springframework.data.mapping.context.MappingContext;

public class BackingAsyncRepository<T, ID> extends AbstractAsyncRepository<ID, T> {

	private final AsyncNamedMap<ID, T> namedMap;
	private final MappingContext<CoherencePersistentEntity<T>, CoherencePersistentProperty> mappingContext;
	private final Class<? extends T> domainType;

	public BackingAsyncRepository(NamedMap<ID, T> namedMap,
			MappingContext<CoherencePersistentEntity<T>, CoherencePersistentProperty> mappingContext,
			Class<? extends T> domainType) {
		this.namedMap = namedMap.async();
		this.mappingContext = mappingContext;
		this.domainType = domainType;
	}

	@SuppressWarnings({"unchecked", "DuplicatedCode"})
	@Override
	protected ID getId(T t) {
		CoherencePersistentEntity<T> entity =
				this.mappingContext.getPersistentEntity(this.domainType);

		if (entity == null) {
			throw new IllegalStateException("Unable to obtain PersistentEntity for type "
					+ this.domainType.getName());
		}

		CoherencePersistentProperty idProp = entity.getRequiredIdProperty();

		try {
			return (ID) (idProp.usePropertyAccess()
					? Objects.requireNonNull(idProp.getGetter()).invoke(t)
					: Objects.requireNonNull(idProp.getField()).get(t));
		}
		catch (Exception ex) {
			throw Base.ensureRuntimeException(ex);
		}
	}

	@Override
	protected Class<? extends T> getEntityType() {
		return this.domainType;
	}

	@Override
	protected AsyncNamedMap<ID, T> getMap() {
		return this.namedMap;
	}

	public CompletableFuture<Long> count() {
		return this.namedMap.size().thenApply(Integer::longValue);
	}

	/**
	 * Deletes a given entity.
	 * @param entity must not be {@literal null}
	 * @return a {@link CompletableFuture}
	 * @throws IllegalArgumentException in case the given entity is {@literal null}.
	 */
	public CompletableFuture<Void> delete(T entity) {
		return this.remove(entity).thenApply((unused) -> null);
	}

	/**
	 * Deletes the entity with the given id.
	 * @param id must not be {@literal null}
	 * @return a {@link CompletableFuture}
	 * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
	 */
	public CompletableFuture<Void> deleteById(ID id) {
		return this.removeById(id).thenApply((unused) -> null);
	}

	/**
	 * Deletes the given entities.
	 * @param entities must not be {@literal null}. Must not contain {@literal null} elements
	 * @return a {@link CompletableFuture}
	 * @throws IllegalArgumentException in case the given {@literal entities} or one of its entities is {@literal null}.
	 */
	public CompletableFuture<Boolean> deleteAll(Iterable<? extends T> entities) {
		return this.removeAll(StreamSupport.stream(entities.spliterator(), false));
	}

	/**
	 * Saves all given entities.
	 * @param entities must not be {@literal null} nor must it contain {@literal null}
	 * @param <S> entity type
	 * @return the saved entities; will never be {@literal null}. The returned {@literal Iterable} will have the same size
	 *         as the {@literal Iterable} passed as an argument.
	 * @throws IllegalArgumentException in case the given {@link Iterable entities} or one of its entities is
	 *           {@literal null}.
	 */
	public <S extends T> CompletableFuture<Iterable<S>> saveAll(Iterable<S> entities) {
		return this.saveAll(StreamSupport.stream(entities.spliterator(), false)).thenApply((unused) -> entities);
	}

	/**
	 * Retrieves an entity by its id.
	 * @param id must not be {@literal null}.
	 * @return the entity with the given id or {@literal Optional#empty()} if none found.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	public CompletableFuture<Optional<T>> findById(ID id) {
		return this.get(id).thenApply(Optional::ofNullable);
	}

	/**
	 * Returns all instances of the type.
	 * @return all entities
	 */
	public CompletableFuture<Iterable<T>> findAll() {
		return this.getAll().thenApply((ts) -> (Iterable<T>) ts);
	}

	/**
	 * Returns all instances of the type {@code T} with the given IDs.
	 * <p>
	 * If some or all ids are not found, no entities are returned for these IDs.
	 * <p>
	 * Note that the order of elements in the result is not guaranteed.
	 * @param ids must not be {@literal null} nor contain any {@literal null} values
	 * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
	 *         {@literal ids}.
	 * @throws IllegalArgumentException in case the given {@link Iterable ids} or one of its items is {@literal null}.
	 */
	public CompletableFuture<Iterable<T>> findAllById(Iterable<ID> ids) {
		return getAll(new InKeySetFilter<>(
				Filters.always(),
				StreamSupport.stream(ids.spliterator(), false).collect(Collectors.toSet())))
				.thenApply((ts) -> (Iterable<T>) ts);
	}

	/**
	 * Returns whether an entity with the given id exists.
	 * @param id must not be {@literal null}
	 * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	public CompletableFuture<Boolean> existsById(ID id) {
		return getMap().containsKey(id);
	}

	/**
	 * Deletes all entities managed by the repository.
	 * @return a {@link CompletableFuture}
	 */
	public CompletableFuture<Void> deleteAll() {
		return this.getMap().clear();
	}

	/**
	 * Alias for {@link #remove(Object, boolean)}.
	 * @param entity  the entity to remove
	 * @param fReturn the flag specifying whether to return removed entity
	 * @return deleted entity, iff {@code fReturn == true}; {@code null}
	 * otherwise
	 */
	public CompletableFuture<T> delete(T entity, boolean fReturn) {
		return remove(entity, fReturn);
	}

	/**
	 * Deletes all instances of the type {@code T} with the given IDs.
	 * @param ids must not be {@literal null}. Must not contain {@literal null} elements
	 * @return a {@link CompletableFuture} that can be used to determine whether
	 *         the operation completed
	 */
	public CompletableFuture<Void> deleteAllById(Iterable<? extends ID> ids) {
		return removeAllById(StreamSupport.stream(ids.spliterator(), false)
				.collect(Collectors.toList())).thenApply((unused) -> null);
	}

	/**
	 * Alias for {@link #removeAllById(Collection)}.
	 * @param colIds the identifiers of the entities to remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	public CompletableFuture<Boolean> deleteAllById(Collection<? extends ID> colIds) {
		return removeAllById(colIds);
	}

	/**
	 * Alias for {@link #removeAllById(Collection, boolean)}.
	 * @param colIds  the identifiers of the entities to remove
	 * @param fReturn the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 * entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	public CompletableFuture<Map<ID, T>> deleteAllById(Collection<? extends ID> colIds, boolean fReturn) {
		return removeAllById(colIds, fReturn);
	}

	/**
	 * Alias for {@link #removeAll(Collection)}.
	 * @param colEntities the entities to remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	public CompletableFuture<Boolean> deleteAll(Collection<? extends T> colEntities) {
		return removeAll(colEntities);
	}

	/**
	 * Alias for {@link #removeAll(Collection, boolean)}.
	 * @param colEntities the entities to remove
	 * @param fReturn     the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 * entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	public CompletableFuture<Map<ID, T>> deleteAll(Collection<? extends T> colEntities, boolean fReturn) {
		return removeAll(colEntities, fReturn);
	}

	/**
	 * Alias for {@link #removeAll(Stream)}.
	 * @param strEntities the entities to remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	public CompletableFuture<Boolean> deleteAll(Stream<? extends T> strEntities) {
		return removeAll(strEntities);
	}

	/**
	 * Alias for {@link #removeAll(Stream, boolean)}.
	 * @param strEntities the entities to remove
	 * @param fReturn     the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 * entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	public CompletableFuture<Map<ID, T>> deleteAll(Stream<? extends T> strEntities, boolean fReturn) {
		return removeAll(strEntities, fReturn);
	}

	/**
	 * Alias for {@link #removeAll(Filter)}.
	 * @param filter the criteria that should be used to select entities to
	 *               remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	public CompletableFuture<Boolean> deleteAll(Filter<?> filter) {
		return removeAll(filter);
	}

	/**
	 * Alias for {@link #removeAll(Filter, boolean)}.
	 * @param filter  the criteria that should be used to select entities to
	 *                remove
	 * @param fReturn the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 * entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	public CompletableFuture<Map<ID, T>> deleteAll(Filter<?> filter, boolean fReturn) {
		return removeAll(filter, fReturn);
	}
}
