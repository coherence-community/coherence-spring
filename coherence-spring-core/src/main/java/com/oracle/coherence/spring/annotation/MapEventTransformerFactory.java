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
package com.oracle.coherence.spring.annotation;

import java.lang.annotation.Annotation;

import com.tangosol.util.MapEventTransformer;

/**
 * A factory that produces instances of {@link MapEventTransformer}
 * for a given {@link Annotation}.
 * <p>
 * A {@link MapEventTransformerFactory} is normally a CDI
 * bean that is also annotated with a {@link MapEventTransformerBinding}
 * annotation. Whenever an injection point annotated with the corresponding
 * {@link MapEventTransformerBinding} annotation is encountered the
 * {@link MapEventTransformerFactory} bean's
 * {@link MapEventTransformerFactory#create(Annotation)}
 * method is called to create an instance of a {@link MapEventTransformer}.
 *
 * @param <A> the annotation type that the factory supports
 * @param <K> the type of the event's key
 * @param <V> the type of event's value
 * @param <U> the type of resulting transformed value
 *
 * @author Jonathan Knight
 * @since 1.0
 */
public interface MapEventTransformerFactory<A extends Annotation, K, V, U> {
	/**
	 * Create a {@link MapEventTransformer} instance.
	 * @param annotation the {@link Annotation} that
	 *                   defines the MapEventTransformer
	 * @return a {@link MapEventTransformer} instance
	 */
	MapEventTransformer<K, V, U> create(A annotation);
}
