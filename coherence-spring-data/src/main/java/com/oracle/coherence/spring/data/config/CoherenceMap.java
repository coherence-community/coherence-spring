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

package com.oracle.coherence.spring.data.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tangosol.net.Coherence;
import com.tangosol.net.NamedMap;
import com.tangosol.net.Session;

/**
 * Optional marker for Coherence repositories where the repository itself
 * should use a cache name different from the name that is auto-selected by
 * the runtime based on the repository class name.
 *
 * @author Ryan Lubke
 * @since 3.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CoherenceMap {
	/**
	 * Specifies the name of the Coherence {@link NamedMap} the annotated repository
	 * should use.
	 * @return the name of the Coherence {@link NamedMap} the annotated repository
	 *         should use
	 */
	String value();

	/**
	 * Specifies the name of the {@link Session} should be used to look up
	 * the {@link NamedMap}.
	 * @return the name of the {@link Session} should be used to look up
	 * 	      the {@link NamedMap}
	 */
	String session() default Coherence.DEFAULT_SCOPE;
}
