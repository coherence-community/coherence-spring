/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.support;

import java.io.Serializable;
import java.util.Locale;

import com.oracle.coherence.spring.data.config.CoherenceMap;
import com.oracle.coherence.spring.data.core.mapping.CoherenceMappingContext;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.util.Assert;

/**
 * Factory responsible for creating Repository instances for a specific repository
 * interface.
 *
 * @param <ID> the entity's identity type
 * @param <T>  the repository type
 * @param <S>  the entity type
 * @author Ryan Lubke
 * @author Gunnar Hillert
 * @since 3.0.0
 */
public class CoherenceRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
		extends RepositoryFactoryBeanSupport<T, S, ID> {

	/**
	 * The {@link Coherence} instance.
	 */
	private Coherence coherence;

	/**
	 * The {@link MappingContext}.
	 */
	private final CoherenceMappingContext coherenceMappingContext = new CoherenceMappingContext();

	/**
	 * Constructs a new {@code CoherenceRepositoryFactoryBean} for the provided repository interface.
	 * @param repositoryInterface the repository interface
	 */
	public CoherenceRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
		super(repositoryInterface);
		setMappingContext(this.coherenceMappingContext);
	}

	/**
	 * First checks for the presence of the {@link CoherenceMap} annotation to
	 * find the name of the Coherence {@link NamedMap} and {@link Session}.  If the
	 * annotation is not present, then if the repository class name itself follows
	 * the format of [Entity-Type]Repository (e.g., BookRepository), then the {@link NamedMap}
	 * will be looked up using the lower-case name of the entity type.  Using the
	 * {@code BookRepository} example, the {@link NamedMap} would be resolved to {@literal book}.
	 * @return {@inheritDoc}
	 * @throws IllegalStateException if the {@link NamedMap} name cannot be resolved
	 */
	@Override
	protected RepositoryFactorySupport createRepositoryFactory() {
		Class<?> repoClass = getObjectType();
		String mapName;
		String sessionName = Coherence.DEFAULT_NAME;
		if (repoClass.isAnnotationPresent(CoherenceMap.class)) {
			CoherenceMap repository = repoClass.getAnnotation(CoherenceMap.class);
			mapName = repository.value();
			sessionName = repository.session();
		}
		else {
			String repoClassName = repoClass.getSimpleName();
			int idx = repoClassName.lastIndexOf("Repository");
			if (idx != -1) {
				mapName = repoClassName.substring(0, idx).toLowerCase(Locale.ROOT);
			}
			else {
				throw new IllegalStateException(String.format("Unable to determine which NamedMap to use." +
						"  Please annotate the Repository interface, %s, with com.coherence.spring.data.config.CoherenceMap" +
						" and specify the name and/or session the repository should use.", repoClass.getName()));
			}
		}
		return new CoherenceRepositoryFactory(this.coherence, this.coherenceMappingContext, sessionName, mapName);
	}

	/**
	 * Configures the {@link Coherence} instance to be used.
	 * @param coherence the Coherence instance to set
	 */
	public void setCoherence(Coherence coherence) {
		this.coherence = coherence;
	}

	/**
	 * Ensure that {@link #coherence} is not null.
	 * @see RepositoryFactoryBeanSupport#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Assert.notNull(this.coherence != null, "Coherence must not be null.");
	}
}
