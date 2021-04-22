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
package com.oracle.coherence.spring.data.model.repositories;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.oracle.coherence.spring.data.config.CoherenceMap;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.BookProjection;
import com.oracle.coherence.spring.data.model.CalendarProjection;
import com.oracle.coherence.spring.data.model.NestedBookProjection;
import com.oracle.coherence.spring.data.model.NestedOpenBookProjection;
import com.oracle.coherence.spring.data.model.OpenBookProjection;
import com.tangosol.util.UUID;

import org.springframework.data.repository.CrudRepository;

@CoherenceMap("book")
public interface BookProjectionRepository extends CrudRepository<Book, UUID> {

	List<BookProjection> findByPages(int pages);

	List<OpenBookProjection> findByTitle(String title);

	List<NestedBookProjection> findByTitleContains(String keyword);

	List<NestedOpenBookProjection> findByTitleEndingWith(String keyword);

	List<CalendarProjection> findByPublished(Calendar date);

	<T> Collection<T> findByTitle(String title, Class<T> type);

	List<Map> findByTitleStartingWith(String keyword);
}
