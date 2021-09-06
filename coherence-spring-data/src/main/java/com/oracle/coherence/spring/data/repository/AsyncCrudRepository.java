/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.repository.Repository;

/**
 * Asynchronous version of {@link org.springframework.data.repository.CrudRepository}.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 *
 * @author Ryan Lubke
 * @since 3.0
 */
public interface AsyncCrudRepository<T, ID> extends Repository<T, ID> {

	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * @param entity must not be {@literal null}
	 * @param <S> the entity type
	 * @return the saved entity; will never be {@literal null}
	 * @throws IllegalArgumentException in case the given {@literal entity} is {@literal null}
	 */
	<S extends T> CompletableFuture<S> save(S entity);

	/**
	 * Saves all given entities.
	 * @param entities must not be {@literal null} nor must it contain {@literal null}
	 * @param <S> the entity type
	 * @return the saved entities; will never be {@literal null}. The returned {@literal Iterable} will have the same size
	 *         as the {@literal Iterable} passed as an argument
	 * @throws IllegalArgumentException in case the given {@link Iterable entities} or one of its entities is
	 *           {@literal null}
	 */
	<S extends T> CompletableFuture<Iterable<S>> saveAll(Iterable<S> entities);

	/**
	 * Retrieves an entity by its id.
	 * @param id must not be {@literal null}
	 * @return the entity with the given id or {@literal Optional#empty()} if none found
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}
	 */
	CompletableFuture<Optional<T>> findById(ID id);

	/**
	 * Returns whether an entity with the given id exists.
	 * @param id must not be {@literal null}
	 * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}
	 */
	CompletableFuture<Boolean> existsById(ID id);

	/**
	 * Returns all instances of the type.
	 * @return all entities
	 */
	CompletableFuture<Iterable<T>> findAll();

	/**
	 * Returns all instances of the type {@code T} with the given IDs.
	 * <p>
	 * If some or all ids are not found, no entities are returned for these IDs.
	 * <p>
	 * Note that the order of elements in the result is not guaranteed.
	 * @param ids must not be {@literal null} nor contain any {@literal null} values
	 * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
	 *         {@literal ids}
	 * @throws IllegalArgumentException in case the given {@link Iterable ids} or one of its items is {@literal null}
	 */
	CompletableFuture<Iterable<T>> findAllById(Iterable<ID> ids);

	/**
	 * Returns the number of entities available.
	 * @return the number of entities
	 */
	CompletableFuture<Long> count();

	/**
	 * Deletes the entity with the given id.
	 * @param id must not be {@literal null}
	 * @return a CompletableFuture that can be used to determine whether the operation completed
	 * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
	 */
	CompletableFuture<Void> deleteById(ID id);

	/**
	 * Deletes a given entity.
	 * @param entity must not be {@literal null}
	 * @return a CompletableFuture that can be used to determine whether the operation completed
	 * @throws IllegalArgumentException in case the given entity is {@literal null}
	 */
	CompletableFuture<Void> delete(T entity);

	/**
	 * Deletes all instances of the type {@code T} with the given IDs.
	 * @param ids must not be {@literal null}. Must not contain {@literal null} elements
	 * @return a CompletableFuture that can be used to determine whether the operation completed
	 * @throws IllegalArgumentException in case the given {@literal ids} or one of its elements is {@literal null}
	 */
	CompletableFuture<Void> deleteAllById(Iterable<? extends ID> ids);

	/**
	 * Deletes the given entities.
	 * @param entities must not be {@literal null}. Must not contain {@literal null} elements
	 * @return a CompletableFuture that can be used to determine whether the operation completed
	 * @throws IllegalArgumentException in case the given {@literal entities} or one of its entities is {@literal null}
	 */
	CompletableFuture<Void> deleteAll(Iterable<? extends T> entities);

	/**
	 * Deletes all entities managed by the repository.
	 * @return a CompletableFuture that can be used to determine whether the operation completed
	 */
	CompletableFuture<Void> deleteAll();
}
