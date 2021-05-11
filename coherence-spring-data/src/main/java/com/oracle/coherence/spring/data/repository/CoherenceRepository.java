/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Stream;

import com.oracle.coherence.repository.AbstractRepository;
import com.oracle.coherence.repository.EntityFactory;
import com.tangosol.util.Filter;
import com.tangosol.util.Fragment;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.ValueUpdater;
import com.tangosol.util.function.Remote;
import com.tangosol.util.stream.RemoteCollector;
import com.tangosol.util.stream.RemoteCollectors;
import com.tangosol.util.stream.RemoteStream;

import org.springframework.data.repository.CrudRepository;

/**
 * Coherence-specific {@link org.springframework.data.repository.Repository} interface.
 *
 * @param <T> the domain type the repository manages
 * @param <ID> the type of the id of the entity the repository manages
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
public interface CoherenceRepository<T, ID>
		extends CrudRepository<T, ID>, ListenerSupport<T, ID> {

	// ----- CRUD support ---------------------------------------------------

	@Override
	long count();

	/**
	 * Return the number of entities in this repository that satisfy specified
	 * filter.
	 * @param filter  the filter to evaluate
	 * @return the number of entities in this repository that satisfy specified
	 *         filter
	 */
	long count(Filter<?> filter);

	@Override
	void delete(T entity);

	/**
	 * Delete specified entity.
	 * @param entity  the entity to remove
	 * @param fReturn the flag specifying whether to return removed entity
	 * @return removed entity, iff {@code fReturn == true}; {@code null}
	 *         otherwise
	 */
	T delete(T entity, boolean fReturn);

	@Override
	void deleteAll();

	/**
	 * Delete specified entities.
	 * @param colEntities the entities to remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	boolean deleteAll(Collection<? extends T> colEntities);

	/**
	 * Delete specified entities.
	 * @param colEntities the entities to remove
	 * @param fReturn     the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 *         entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	Map<ID, T> deleteAll(Collection<? extends T> colEntities, boolean fReturn);

	@Override
	void deleteAll(Iterable<? extends T> entities);

	/**
	 * Delete all entities based on the specified criteria.
	 * @param filter the criteria that should be used to select entities to
	 *               remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	boolean deleteAll(Filter<?> filter);

	/**
	 * Remove all entities based on the specified criteria.
	 * @param filter  the criteria that should be used to select entities to
	 *                remove
	 * @param fReturn the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 *         entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	Map<ID, T> deleteAll(Filter<?> filter, boolean fReturn);

	/**
	 * Delete specified entities.
	 * @param strEntities the entities to remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	boolean deleteAll(Stream<? extends T> strEntities);

	/**
	 * Delete specified entities.
	 * @param strEntities the entities to remove
	 * @param fReturn     the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 *         entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	Map<ID, T> deleteAll(Stream<? extends T> strEntities, boolean fReturn);

	/**
	 * Delete entities with the specified identifiers.
	 * @param colIds the identifiers of the entities to remove
	 * @return {@code true} if this repository changed as a result of the call
	 */
	boolean deleteAllById(Collection<? extends ID> colIds);

	/**
	 * Delete entities with the specified identifiers.
	 * @param colIds  the identifiers of the entities to remove
	 * @param fReturn the flag specifying whether to return removed entity
	 * @return the map of removed entity identifiers as keys, and the removed
	 *         entities as values iff {@code fReturn == true}; {@code null} otherwise
	 */
	Map<ID, T> deleteAllById(Collection<? extends ID> colIds, boolean fReturn);

	@Override
	void deleteById(ID id);

	@Override
	boolean existsById(ID id);

	@Override
	Iterable<T> findAll();

	@Override
	Iterable<T> findAllById(Iterable<ID> ids);

	@Override
	Optional<T> findById(ID id);

	/**
	 * Return the value extracted from an entity with a given identifier.
	 * <p>
	 * For example, you could extract {@code Person}'s {@code name} attribute by
	 * calling a getter on a remote {@code Person} entity instance:
	 * <pre>
	 *     people.get(ssn, Person::getName);
	 * </pre>
	 * <p>
	 * You could also extract a {@link Fragment} containing the {@code Person}'s
	 * {@code name} and {@code age} attributes by calling corresponding getters
	 * on the remote {@code Person} entity instance:
	 * <pre>
	 *     Fragment&lt;Person&gt; person = people.get(ssn, Extractors.fragment(Person::getName, Person::getAge));
	 *     System.out.println("name: " + person.get(Person::getName));
	 *     System.out.println(" age: " + person.get(Person::getAge));
	 * </pre>
	 * <p>
	 * Finally, you can also extract nested fragments:
	 * <pre>
	 *     Fragment&lt;Person&gt; person = people.get(ssn,
	 *           Extractors.fragment(Person::getName, Person::getAge,
	 *                               Extractors.fragment(Person::getAddress, Address::getCity, Address::getState));
	 *     System.out.println(" name: " + person.get(Person::getName));
	 *     System.out.println("  age: " + person.get(Person::getAge));
	 *
	 *     Fragment&lt;Address&gt; address = person.getFragment(Person::getAddress);
	 *     System.out.println(" city: " + address.get(Address::getCity));
	 *     System.out.println("state: " + address.get(Address::getState));
	 * </pre>
	 * Note that the actual extraction (via the invocation of the specified
	 * getter method) will happen on the primary owner for the specified entity,
	 * and only the extracted value will be sent over the network to the client,
	 * which can significantly reduce the amount of data transferred.
	 * @param id        the entity's identifier
	 * @param extractor the {@link ValueExtractor} to extract value with
	 * @param <R>       the type of the extracted value
	 * @return the extracted value
	 */
	<R> R get(ID id, ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return a map of values extracted from a set of entities with the given
	 * identifiers.
	 * @param colIds     the entity identifiers
	 * @param extractor  the {@link ValueExtractor} to extract values with
	 * @param <R>        the type of the extracted values
	 * @return the map of extracted values, keyed by entity id
	 * @see #get(Object, ValueExtractor)
	 */
	<R> Map<ID, R> getAll(Collection<? extends ID> colIds, ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return all entities that satisfy the specified criteria.
	 * @param filter  the criteria to evaluate
	 * @return all entities that satisfy the specified criteria
	 */
	Collection<T> getAll(Filter<?> filter);

	/**
	 * Return a map of values extracted from a set of entities based on the
	 * specified criteria.
	 * @param filter     the criteria to use to select entities for extraction
	 * @param extractor  the {@link ValueExtractor} to extract values with
	 * @param <R>        the type of the extracted values
	 * @return the map of extracted values, keyed by entity id
	 * @see #get(Object, ValueExtractor)
	 */
	<R> Map<ID, R> getAll(Filter<?> filter, ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return a map of values extracted from all entities in the repository.
	 * @param extractor  the {@link ValueExtractor} to extract values with
	 * @param <R>        the type of the extracted values
	 * @return the map of extracted values, keyed by entity id
	 * @see #get(Object, ValueExtractor)
	 */
	<R> Map<ID, R> getAll(ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return all entities that satisfy the specified criteria, sorted using
	 * specified {@link Remote.Comparator}.
	 * @param filter   the criteria to evaluate
	 * @param orderBy  the comparator to sort the results with
	 * @return all entities that satisfy specified criteria, sorted using
	 * specified {@link Remote.Comparator}.
	 */
	Collection<T> getAllOrderedBy(Filter<?> filter, Remote.Comparator<? super T> orderBy);

	/**
	 * Return all entities that satisfy the specified criteria, sorted using
	 * specified {@link Comparable} attribute.
	 * @param filter   the criteria to evaluate
	 * @param orderBy  the {@link Comparable} attribute to sort the results by
	 * @param <R>      the type of the extracted values
	 * @return all entities that satisfy specified criteria, sorted using
	 *         specified {@link Comparable} attribute.
	 */
	<R extends Comparable<? super R>> Collection<T> getAllOrderedBy(Filter<?> filter, ValueExtractor<? super T, ? extends R> orderBy);

	/**
	 * Return all entities in this repository, sorted using
	 * specified {@link Remote.Comparator}.
	 * @param orderBy  the comparator to sort the results with
	 * @return all entities in this repository, sorted using
	 *         specified {@link Remote.Comparator}.
	 */
	Collection<T> getAllOrderedBy(Remote.Comparator<? super T> orderBy);

	/**
	 * Return all entities in this repository, sorted using
	 * specified {@link Comparable} attribute.
	 * @param orderBy  the {@link Comparable} attribute to sort the results by
	 * @param <R>      the type of the extracted values
	 * @return all entities in this repository, sorted using
	 *         specified {@link Comparable} attribute.
	 */
	<R extends Comparable<? super R>> Collection<T> getAllOrderedBy(ValueExtractor<? super T, ? extends R> orderBy);

	@Override
	<S extends T> S save(S entity);

	@Override
	<S extends T> Iterable<S> saveAll(Iterable<S> entities);

	/**
	 * Store all specified entities as a batch.
	 * @param strEntities  the entities to store
	 */
	void saveAll(Stream<? extends T> strEntities);

	/**
	 * Update an entity using specified updater and the new value.
	 * <p>
	 * For example, you could update {@code Person}'s {@code age} attribute by
	 * calling a setter on a remote {@code Person} entity instance:
	 * <pre>
	 *     people.update(ssn, Person::setAge, 21);
	 * </pre>
	 * Note that the actual update (via the invocation of the specified setter
	 * method) will happen on the primary owner for the specified entity, and
	 * the updater will have exclusive access to an entity during the
	 * execution.
	 * @param id      the entity's identifier
	 * @param updater the updater function to use
	 * @param value   the value to update entity with, which will be passed as
	 *                an argument to the updater function
	 * @param <U>     the type of value to update
	 */
	<U> void update(ID id, ValueUpdater<? super T, ? super U> updater, U value);

	/**
	 * Update an entity using specified updater and the new value, and optional
	 * {@link EntityFactory} that will be used to create entity instance if it
	 * doesn't already exist in the repository.
	 * <p>
	 * For example, you could update {@code Person}'s {@code age} attribute by
	 * calling a setter on a remote {@code Person} entity instance:
	 * <pre>
	 *     people.update(ssn, Person::setAge, 21, Person::new);
	 * </pre>
	 * If the person with the specified identifier does not exist, the {@link
	 * EntityFactory} will be used to create a new instance. In the example
	 * above, it will invoke a constructor on the {@code Person} class that
	 * takes identifier as an argument.
	 * <p>
	 * Note that the actual update (via the invocation of the specified setter
	 * method) will happen on the primary owner for the specified entity, and
	 * the updater will have exclusive access to an entity during the
	 * execution.
	 * @param id      the entity's identifier
	 * @param updater the updater function to use
	 * @param value   the value to update entity with, which will be passed as
	 *                an argument to the updater function
	 * @param <U>     the type of value to update
	 * @param factory the entity factory to use to create new entity instance
	 */
	<U> void update(ID id, ValueUpdater<? super T, ? super U> updater, U value,
			EntityFactory<? super ID, ? extends T> factory);

	/**
	 * Update an entity using specified updater function.
	 * <p>
	 * For example, you could increment {@code Person}'s {@code age} attribute
	 * and return the updated {@code Person} entity:
	 * <pre>
	 *    people.update(ssn, person -&gt;
	 *        {
	 *        person.setAge(person.getAge() + 1);
	 *        return person;
	 *        });
	 * </pre>
	 * This variant of the {@code update} method offers ultimate flexibility, as
	 * it allows you to return any value you want as the result of the
	 * invocation, at the cost of typically slightly more complex logic at the
	 * call site.
	 * <p>
	 * Note that the actual update (via the evaluation of the specified
	 * function) will happen on the primary owner for the specified entity, and
	 * the updater will have exclusive access to an entity during the
	 * execution.
	 * @param id      the entity's identifier
	 * @param updater the updater function to use
	 * @param <R>     the type of return value of the updater function
	 * @return the result of updater function evaluation
	 */
	<R> R update(ID id, Remote.Function<? super T, ? extends R> updater);

	/**
	 * Update an entity using specified updater function, and optional {@link
	 * EntityFactory} that will be used to create entity instance if it doesn't
	 * already exist in the repository.
	 * <p>
	 * For example, you could increment {@code Person}'s {@code age} attribute
	 * and return the updated {@code Person} entity:
	 * <pre>
	 *    people.update(ssn, person -&gt;
	 *        {
	 *        person.setAge(person.getAge() + 1);
	 *        return person;
	 *        }, Person::new);
	 * </pre>
	 * If the person with the specified identifier does not exist, the {@link
	 * EntityFactory} will be used to create a new instance. In the example
	 * above, it will invoke a constructor on the {@code Person} class that
	 * takes identifier as an argument.
	 * <p>
	 * This variant of the {@code update} method offers ultimate flexibility, as
	 * it allows you to return any value you want as the result of the
	 * invocation, at the cost of typically slightly more complex logic at the
	 * call site.
	 * <p>
	 * Note that the actual update (via the evaluation of the specified
	 * function) will happen on the primary owner for the specified entity, and
	 * the updater will have exclusive access to an entity during the
	 * execution.
	 * @param id      the entity's identifier
	 * @param updater the updater function to use
	 * @param factory the entity factory to use to create new entity instance
	 * @param <R>     the type of return value of the updater function
	 * @return the result of updater function evaluation
	 */
	<R> R update(ID id, Remote.Function<? super T, ? extends R> updater,
			EntityFactory<? super ID, ? extends T> factory);

	/**
	 * Update an entity using specified updater and the new value.
	 * <p>
	 * Unlike {@link #update(Object, ValueUpdater, Object)}, which doesn't
	 * return anything, this method is typically used to invoke "fluent" methods
	 * on the target entity that return entity itself (although they are free to
	 * return any value they want).
	 * <p>
	 * For example, you could use it to add an item to the {@code ShoppingCart}
	 * entity and return the updated {@code ShoppingCart} instance in a single
	 * call:
	 * <pre>
	 *     Item item = ...
	 *     ShoppingCart cart = carts.update(cartId, ShoppingCart::addItem, item);
	 * </pre>
	 * Note that the actual update (via the invocation of the specified setter
	 * method) will happen on the primary owner for the specified entity, and
	 * the updater will have exclusive access to an entity during the
	 * execution.
	 * @param id      the entity's identifier
	 * @param updater the updater function to use
	 * @param value   the value to update entity with, which will be passed as
	 *                an argument to the updater function
	 * @param <U>     the type of value to update
	 * @param <R>     the type of return value of the updater function
	 * @return the result of updater function evaluation
	 */
	<U, R> R update(ID id, Remote.BiFunction<? super T, ? super U, ? extends R> updater,
			U value);

	/**
	 * Update an entity using specified updater function, and optional {@link
	 * EntityFactory} that will be used to create entity instance if it doesn't
	 * already exist in the repository.
	 * <p>
	 * Unlike {@link #update(Object, ValueUpdater, Object)}, which doesn't
	 * return anything, this method is typically used to invoke "fluent" methods
	 * on the target entity that return entity itself (although they are free to
	 * return any value they want).
	 * <p>
	 * For example, you could use it to add an item to the {@code ShoppingCart}
	 * entity and return the updated {@code ShoppingCart} instance in a single
	 * call:
	 * <pre>
	 *     Item item = ...
	 *     ShoppingCart cart = carts.update(cartId, ShoppingCart::addItem, item, ShoppingCart::new);
	 * </pre>
	 * If the cart with the specified identifier does not exist, the specified
	 * {@link EntityFactory} will be used to create a new instance. In the
	 * example above, it will invoke a constructor on the {@code ShoppingCart}
	 * class that takes identifier as an argument.
	 * <p>
	 * Note that the actual update (via the evaluation of the specified
	 * function) will happen on the primary owner for the specified entity, and
	 * the updater will have exclusive access to an entity during the
	 * execution.
	 * @param id      the entity's identifier
	 * @param updater the updater function to use
	 * @param value   the value to update entity with, which will be passed as
	 *                an argument to the updater function
	 * @param factory the entity factory to use to create new entity instance
	 * @param <U>     the type of value to update
	 * @param <R>     the type of return value of the updater function
	 * @return the result of updater function evaluation
	 */
	<U, R> R update(ID id, Remote.BiFunction<? super T, ? super U, ? extends R> updater, U value,
			EntityFactory<? super ID, ? extends T> factory);

	/**
	 * Update multiple entities using specified updater and the new value.
	 * @param filter  the criteria to use to select entities to update
	 * @param updater the updater function to use
	 * @param value   the value to update each entity with, which will be passed
	 *                as an argument to the updater function
	 * @param <U>     the type of value to update
	 */
	<U> void updateAll(Filter<?> filter, ValueUpdater<? super T, ? super U> updater, U value);

	/**
	 * Update multiple entities using specified updater function.
	 * @param filter  the criteria to use to select entities to update
	 * @param updater the updater function to use
	 * @param <R>     the type of return value of the updater function
	 * @return a map of updater function results, keyed by entity id
	 */
	<R> Map<ID, R> updateAll(Filter<?> filter, Remote.Function<? super T, ? extends R> updater);

	/**
	 * Update multiple entities using specified updater and the new value.
	 * @param filter  the criteria to use to select entities to update
	 * @param updater the updater function to use
	 * @param value   the value to update each entity with, which will be passed
	 *                as an argument to the updater function
	 * @param <U>     the type of value to update
	 * @param <R>     the type of return value of the updater function
	 * @return a map of updater function results, keyed by entity id
	 */
	<U, R> Map<ID, R> updateAll(Filter<?> filter,
			Remote.BiFunction<? super T, ? super U, ? extends R> updater, U value);

	// ---- Stream API support ----------------------------------------------

	/**
	 * Return a stream of all entities in this repository.
	 * @return a stream of all entities in this repository
	 */
	RemoteStream<T> stream();

	/**
	 * Return a stream of entities with the specified identifiers.
	 * @param colIds  the identifiers of the entities to include in the
	 *                returned stream
	 * @return a stream of entities for the specified identifiers
	 */
	RemoteStream<T> stream(Collection<? extends ID> colIds);

	/**
	 * Return a stream of all entities in this repository that satisfy the
	 * specified criteria.
	 * @param filter  the criteria an entity must satisfy in order to be
	 *                included in the returned stream
	 * @return a stream of entities that satisfy the specified criteria
	 */
	RemoteStream<T> stream(Filter<?> filter);

	// ---- aggregation support ---------------------------------------------

	/**
	 * Return the average of the specified function.
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the average of the specified function
	 */
	double average(Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the average of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the average of the specified function
	 */
	double average(Filter<?> filter, Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the average of the specified function.
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the average of the specified function
	 */
	double average(Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the average of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the average of the specified function
	 */
	double average(Filter<?> filter, Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the average of the specified function.
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the average of the specified function
	 */
	double average(Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the average of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the average of the specified function
	 */
	double average(Filter<?> filter, Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the average of the specified function.
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the average of the specified function
	 */
	BigDecimal average(Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the average of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to average;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the average of the specified function
	 */
	BigDecimal average(Filter<?> filter, Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the set of distinct values for the specified extractor.
	 * @param extractor  the extractor to get a value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getName}
	 * @param <R>        the type of extracted values
	 * @return the set of distinct values for the specified extractor
	 */
	<R> Collection<? extends R> distinct(ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return the set of distinct values for the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get a value from;
	 * @param <R>        the type of extracted values
	 * @return the set of distinct values for the specified extractor
	 */
	<R> Collection<? extends R> distinct(Filter<?> filter,
			ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return the grouping of entities by the specified extractor.
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param <K>        the type of extracted grouping keys
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be sets of entities
	 *         that match each extracted key
	 */
	<K> Map<K, Set<T>> groupBy(ValueExtractor<? super T, ? extends K> extractor);

	/**
	 * Return the grouping of entities by the specified extractor, ordered by
	 * the specified attribute within each group.
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param orderBy    the {@link Remote.Comparator} to sort the results
	 *                   within each group by
	 * @param <K>        the type of extracted grouping keys
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be sorted sets
	 *         of entities that match each extracted key
	 */
	<K> Map<K, SortedSet<T>> groupBy(ValueExtractor<? super T, ? extends K> extractor,
			Remote.Comparator<? super T> orderBy);

	/**
	 * Return the grouping of entities by the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param <K>        the type of extracted grouping keys
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be sets of entities
	 *         that match each extracted key
	 */
	<K> Map<K, Set<T>> groupBy(Filter<?> filter, ValueExtractor<? super T, ? extends K> extractor);

	/**
	 * Return the grouping of entities by the specified extractor, ordered by
	 * the specified attribute within each group.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param orderBy    the {@link Remote.Comparator} to sort the results
	 *                   within each group by
	 * @param <K>        the type of extracted grouping keys
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be sorted sets
	 *         of entities that match each extracted key
	 */
	<K> Map<K, SortedSet<T>> groupBy(Filter<?> filter, ValueExtractor<? super T, ? extends K> extractor,
			Remote.Comparator<? super T> orderBy);

	/**
	 * Return the grouping of entities by the specified extractor.
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param collector  the {@link RemoteCollector} to apply to grouped entities
	 * @param <K>        the type of extracted grouping keys
	 * @param <A>        the type of collector's accumulator
	 * @param <R>        the type of collector's result
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be results of
	 *         the specified {@code collector} for each group
	 * @see RemoteCollectors
	 */
	<K, A, R> Map<K, R> groupBy(ValueExtractor<? super T, ? extends K> extractor,
			RemoteCollector<? super T, A, R> collector);

	/**
	 * Return the grouping of entities by the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param collector  the {@link RemoteCollector} to apply to grouped entities
	 * @param <K>        the type of extracted grouping keys
	 * @param <A>        the type of collector's accumulator
	 * @param <R>        the type of collector's result
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be results of
	 *         the specified {@code collector} for each group
	 * @see RemoteCollectors
	 */
	<K, A, R> Map<K, R> groupBy(Filter<?> filter, ValueExtractor<? super T, ? extends K> extractor,
			RemoteCollector<? super T, A, R> collector);

	/**
	 * Return the grouping of entities by the specified extractor.
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param mapFactory the supplier to use to create result {@code Map}
	 * @param collector  the {@link RemoteCollector} to apply to grouped entities
	 * @param <K>        the type of extracted grouping keys
	 * @param <A>        the type of collector's accumulator
	 * @param <R>        the type of collector's result
	 * @param <M>        the type of result {@code Map}
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be results of
	 *         the specified {@code collector} for each group
	 * @see RemoteCollectors
	 */
	<K, A, R, M extends Map<K, R>> M groupBy(ValueExtractor<? super T, ? extends K> extractor,
			Remote.Supplier<M> mapFactory, RemoteCollector<? super T, A, R> collector);

	/**
	 * Return the grouping of entities by the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get a grouping value from;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getGender}
	 * @param mapFactory the supplier to use to create result {@code Map}
	 * @param collector  the {@link RemoteCollector} to apply to grouped entities
	 * @param <K>        the type of extracted grouping keys
	 * @param <A>        the type of collector's accumulator
	 * @param <R>        the type of collector's result
	 * @param <M>        the type of result {@code Map}
	 * @return the the grouping of entities by the specified extractor; the keys
	 *         in the returned map will be distinct values extracted by the
	 *         specified {@code extractor}, and the values will be results of
	 *         the specified {@code collector} for each group
	 * @see RemoteCollectors
	 */
	<K, A, R, M extends Map<K, R>> M groupBy(Filter<?> filter,
			ValueExtractor<? super T, ? extends K> extractor, Remote.Supplier<M> mapFactory,
			RemoteCollector<? super T, A, R> collector);

	/**
	 * Return the maximum value of the specified function.
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the maximum value of the specified function
	 */
	int max(Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the maximum value of the specified function
	 */
	int max(Filter<?> filter, Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the maximum value of the specified function
	 */
	long max(Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the maximum value of the specified function
	 */
	long max(Filter<?> filter, Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the maximum value of the specified function
	 */
	double max(Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the maximum value of the specified function
	 */
	double max(Filter<?> filter, Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the maximum value of the specified function
	 */
	BigDecimal max(Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the maximum value of the specified function
	 */
	BigDecimal max(Filter<?> filter, Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getName}
	 * @param <R>       the type of the extracted values
	 * @return the maximum value of the specified function
	 */
	<R extends Comparable<? super R>> R max(Remote.ToComparableFunction<? super T, R> extractor);

	/**
	 * Return the maximum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getName}
	 * @param <R>        the type of the extracted values
	 * @return the maximum value of the specified function
	 */
	<R extends Comparable<? super R>> R max(Filter<?> filter, Remote.ToComparableFunction<? super T, R> extractor);

	/**
	 * Return the entity with the maximum value of the specified function.
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @param <R>        the type of the extracted values
	 * @return the entity with the maximum value of the specified function
	 */
	<R extends Comparable<? super R>> Optional<T> maxBy(ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return the entity with the maximum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the maximum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @param <R>        the type of the extracted values
	 * @return the entity with the maximum value of the specified function
	 */
	<R extends Comparable<? super R>> Optional<T> maxBy(Filter<?> filter, ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the minimum value of the specified function
	 */
	int min(Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the minimum value of the specified function
	 */
	int min(Filter<?> filter, Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the minimum value of the specified function
	 */
	long min(Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the minimum value of the specified function
	 */
	long min(Filter<?> filter, Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the minimum value of the specified function
	 */
	double min(Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the minimum value of the specified function
	 */
	double min(Filter<?> filter, Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the minimum value of the specified function
	 */
	BigDecimal min(Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the minimum value of the specified function
	 */
	BigDecimal min(Filter<?> filter, Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getName}
	 * @param <R>        the type of the extracted values
	 * @return the minimum value of the specified function
	 */
	<R extends Comparable<? super R>> R min(Remote.ToComparableFunction<? super T, R> extractor);

	/**
	 * Return the minimum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getName}
	 * @param <R>        the type of the extracted values
	 * @return the minimum value of the specified function
	 */
	<R extends Comparable<? super R>> R min(Filter<?> filter, Remote.ToComparableFunction<? super T, R> extractor);

	/**
	 * Return the entity with the minimum value of the specified function.
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @param <R>        the type of the extracted values
	 * @return the entity with the minimum value of the specified function
	 */
	<R extends Comparable<? super R>> Optional<T> minBy(ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return the entity with the minimum value of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to determine the minimum value for;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @param <R>        the type of the extracted values
	 * @return the entity with the minimum value of the specified function
	 */
	<R extends Comparable<? super R>> Optional<T> minBy(Filter<?> filter, ValueExtractor<? super T, ? extends R> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the sum of the specified function
	 */
	long sum(Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the sum of the specified function
	 */
	long sum(Filter<?> filter, Remote.ToIntFunction<? super T> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the sum of the specified function
	 */
	long sum(Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getAge}
	 * @return the sum of the specified function
	 */
	long sum(Filter<?> filter, Remote.ToLongFunction<? super T> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the sum of the specified function
	 */
	double sum(Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getWeight}
	 * @return the sum of the specified function
	 */
	double sum(Filter<?> filter, Remote.ToDoubleFunction<? super T> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the sum of the specified function
	 */
	BigDecimal sum(Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the sum of the specified function.
	 * @param filter     the entity selection criteria
	 * @param extractor  the function to sum;
	 *                   typically a method reference on the entity class,
	 *                   such as {@code Person::getSalary}
	 * @return the sum of the specified function
	 */
	BigDecimal sum(Filter<?> filter, Remote.ToBigDecimalFunction<? super T> extractor);

	/**
	 * Return the top N highest values for the specified extractor.
	 * @param extractor  the extractor to get the values to compare with
	 * @param cResults   the number of highest values to return
	 * @param <R>        the type of the extracted result
	 * @return the top N highest values for the specified extractor
	 */
	<R extends Comparable<? super R>> List<R> top(ValueExtractor<? super T, ? extends R> extractor, int cResults);

	/**
	 * Return the top N highest values for the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get the values to compare with
	 * @param cResults   the number of highest values to return
	 * @param <R>        the type of the extracted result
	 * @return the top N highest values for the specified extractor
	 */
	<R extends Comparable<? super R>> List<R> top(Filter<?> filter, ValueExtractor<? super T, ? extends R> extractor, int cResults);

	/**
	 * Return the top N highest values for the specified extractor.
	 * @param extractor  the extractor to get the values to compare with
	 * @param comparator the comparator to use when comparing extracted values
	 * @param cResults   the number of highest values to return
	 * @param <R>        the type of the extracted result
	 * @return the top N highest values for the specified extractor
	 */
	<R> List<R> top(ValueExtractor<? super T, ? extends R> extractor, Remote.Comparator<? super R> comparator, int cResults);

	/**
	 * Return the top N highest values for the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get the values to compare with
	 * @param comparator the comparator to use when comparing extracted values
	 * @param cResults   the number of highest values to return
	 * @param <R>        the type of the extracted result
	 * @return the top N highest values for the specified extractor
	 */
	<R> List<R> top(Filter<?> filter, ValueExtractor<? super T, ? extends R> extractor, Remote.Comparator<? super R> comparator, int cResults);

	/**
	 * Return the top N entities with the highest values for the specified extractor.
	 * @param extractor  the extractor to get the values to compare with
	 * @param cResults   the number of highest values to return
	 * @param <R>        the type of the extracted result
	 * @return the top N entities with the highest values for the specified extractor
	 */
	<R extends Comparable<? super R>> List<T> topBy(ValueExtractor<? super T, ? extends R> extractor, int cResults);

	/**
	 * Return the top N entities with the highest values for the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param extractor  the extractor to get the values to compare with
	 * @param cResults   the number of highest values to return
	 * @param <R>        the type of values used for comparison
	 * @return the top N entities with the highest values for the specified extractor
	 */
	<R extends Comparable<? super R>> List<T> topBy(Filter<?> filter, ValueExtractor<? super T, ? extends R> extractor, int cResults);

	/**
	 * Return the top N entities with the highest values for the specified extractor.
	 * @param comparator the comparator to use when comparing extracted values
	 * @param cResults   the number of highest values to return
	 * @return the top N entities with the highest values for the specified extractor
	 */
	List<T> topBy(Remote.Comparator<? super T> comparator, int cResults);

	/**
	 * Return the top N entities with the highest values for the specified extractor.
	 * @param filter     the entity selection criteria
	 * @param comparator the comparator to use when comparing extracted values
	 * @param cResults   the number of highest values to return
	 * @return the top N entities with the highest values for the specified extractor
	 */
	List<T> topBy(Filter<?> filter, Remote.Comparator<? super T> comparator, int cResults);

	// ----- listener support -----------------------------------------------

	@Override
	void addListener(AbstractRepository.Listener<? super T> listener);

	@Override
	void removeListener(AbstractRepository.Listener<? super T> listener);

	@Override
	void addListener(ID id, AbstractRepository.Listener<? super T> listener);

	@Override
	void removeListener(ID id, AbstractRepository.Listener<? super T> listener);

	@Override
	void addListener(Filter<?> filter, AbstractRepository.Listener<? super T> listener);

	@Override
	void removeListener(Filter<?> filter, AbstractRepository.Listener<? super T> listener);

	@Override
	AbstractRepository.Listener.Builder<T> listener();
}
