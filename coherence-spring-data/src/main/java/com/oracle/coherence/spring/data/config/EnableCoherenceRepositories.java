/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.oracle.coherence.spring.data.repository.BackingRepository;
import com.oracle.coherence.spring.data.support.CoherenceRepositoryFactoryBean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;

/**
 * Annotation to enable Coherence repositories. If no base package is configured through either {@link #value()},
 * {@link #basePackages()} or {@link #basePackageClasses()} it will trigger scanning of the package of annotated class.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(CoherenceRepositoriesRegistrar.class)
public @interface EnableCoherenceRepositories {
	/**
	 * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation declarations
	 * e.g.: {@code @EnableCoherenceRepositories("org.my.pkg")} instead of
	 * {@code @EnableCoherenceRepositories(basePackages="org.my.pkg")}.*
	 *
	 * @return the base packages to scan; an empty array by default
	 */
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually
	 * exclusive with) this attribute. Use {@link #basePackageClasses()} for a type-safe alternative
	 * to String-based package names.
	 *
	 * @return the base packages to scan; an empty array by default
	 */
	String[] basePackages() default {};

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for
	 * annotated components. The package of each class specified will be scanned. Consider creating
	 * a special no-op marker class or interface in each package that serves no purpose other
	 * than being referenced by this attribute.
	 *
	 * @return the base package classes to scan; an empty array by default
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Specifies which types are eligible for component scanning. Further narrows the set of candidate
	 * components from everything in {@link #basePackages()} to everything in the base packages that
	 * matches the given filter or filters.
	 *
	 * @return the include filters to apply during scanning; an empty array by default
	 */
	ComponentScan.Filter[] includeFilters() default {};

	/**
	 * Specifies which types are not eligible for component scanning.
	 *
	 * @return the exclude filters to apply during scanning; an empty array by default
	 */
	ComponentScan.Filter[] excludeFilters() default {};

	/**
	 * Returns the postfix to be used when looking up custom repository implementations. Defaults
	 * to {@literal Impl}. So for a repository named {@code PersonRepository} the corresponding
	 * implementation class will be looked up scanning for {@code PersonRepositoryImpl}.
	 *
	 * @return {@literal Impl} by default.
	 */
	String repositoryImplementationPostfix() default "Impl";

	/**
	 * Configures the location of where to find the Spring Data named queries properties file. Will default to
	 * {@code META-INFO/mongo-named-queries.properties}.
	 *
	 * @return empty {@link String} by default.
	 */
	String namedQueriesLocation() default "";

	/**
	 * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query
	 * methods. Defaults to {@link QueryLookupStrategy.Key#CREATE_IF_NOT_FOUND}.
	 *
	 * @return {@link QueryLookupStrategy.Key#CREATE_IF_NOT_FOUND} by default.
	 */
	QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

	/**
	 * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
	 * {@link CoherenceRepositoryFactoryBean}.
	 *
	 * @return {@link CoherenceRepositoryFactoryBean} by default.
	 */
	Class<?> repositoryFactoryBeanClass() default CoherenceRepositoryFactoryBean.class;

	/**
	 * Configure the repository base class to be used to create repository proxies for this particular
	 * configuration.
	 *
	 * @return {@link DefaultRepositoryBaseClass} by default.
	 */
	Class<?> repositoryBaseClass() default BackingRepository.class;

	/**
	 * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be
	 * discovered by the repositories infrastructure.
	 *
	 * @return {@literal false} by default.
	 */
	boolean considerNestedRepositories() default false;
}
