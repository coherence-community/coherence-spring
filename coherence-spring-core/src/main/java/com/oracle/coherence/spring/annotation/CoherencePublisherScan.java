/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 */
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.oracle.coherence.spring.messaging.CoherencePublisherScanRegistrar;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

/**
 * Configures component scanning directives for use with
 * {@link org.springframework.context.annotation.Configuration} classes.
 * <p>
 * Scans for {@link CoherencePublisher} on interfaces to create {@code CoherencePublisherProxyFactoryBean}s.
 *
 * @author Artem Bilan
 * @since 3.0
 * @see CoherencePublisher
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(CoherencePublisherScanRegistrar.class)
public @interface CoherencePublisherScan {
	/**
	 * Alias for the {@link #basePackages()} attribute.
	 * Allows for more concise annotation declarations e.g.:
	 * {@code @IntegrationComponentScan("org.my.pkg")} instead of
	 * {@code @IntegrationComponentScan(basePackages="org.my.pkg")}.
	 * @return the array of 'basePackages'.
	 */
	@AliasFor("basePackages")
	String[] value() default { };

	/**
	 * Base packages to scan for annotated components. The {@link #value()} is an alias
	 * for (and mutually exclusive with) this attribute. Use {@link #basePackageClasses()}
	 * for a type-safe alternative to String-based package names.
	 * @return the array of 'basePackages'.
	 */
	@AliasFor("value")
	String[] basePackages() default { };

	/**
	 * Type-safe alternative to {@link #basePackages()} for specifying the packages to
	 * scan for annotated components. The package of each class specified will be scanned.
	 * Consider creating a special no-op marker class or interface in each package that
	 * serves no purpose other than being referenced by this attribute.
	 * @return the array of 'basePackageClasses'.
	 */
	Class<?>[] basePackageClasses() default { };


	/**
	 * Indicates whether automatic detection of classes annotated with
	 * {@code @CoherencePublisher} should be enabled.
	 * @return the {@code useDefaultFilters} flag
	 * @since 5.0
	 */
	boolean useDefaultFilters() default true;

	/**
	 * Specifies which types are eligible for component scanning.
	 * <p>Further narrows the set of candidate components from everything in
	 * {@link #basePackages} to everything in the base packages that matches the given
	 * filter or filters.
	 * <p>Note that these filters will be applied in addition to the default filters, if
	 * specified. Any type under the specified base packages which matches a given filter
	 * will be included, even if it does not match the default filters (i.e. is not
	 * annotated with {@code @CoherencePublisher}).
	 * @return the {@code includeFilters} array
	 * @since 5.0
	 * @see #excludeFilters()
	 */
	ComponentScan.Filter[] includeFilters() default { };

	/**
	 * Specifies which types are not eligible for component scanning.
	 * @return the {@code excludeFilters} array
	 * @since 5.0
	 * @see #includeFilters()
	 */
	ComponentScan.Filter[] excludeFilters() default { };


}
