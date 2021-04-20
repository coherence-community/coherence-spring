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

import java.util.Collection;
import java.util.List;

import com.oracle.coherence.spring.data.model.Author;
import com.oracle.coherence.spring.data.model.Book;
import com.tangosol.util.UUID;

import org.springframework.data.repository.CrudRepository;


public interface BookRepository extends CrudRepository<Book, UUID> {

	boolean existsByAuthor(Author author);

	List<Book> findByAuthor(Author author);

	List<Book> findByPagesGreaterThanEqual(int pages);

	List<Book> findByPagesLessThanEqual(int pageCount);

	List<Book> findByTitleLike(String like);

	List<Book> findByPagesGreaterThan(int pageCount);

	List<Book> findByPagesLessThan(int pageCount);

	List<Book> findByPublicationYearAfter(int year);

	List<Book> findByPublicationYearBefore(int year);

	List<Book> findByTitleContains(String keyword);

	List<Book> findByTitleStartingWith(String keyword);

	List<Book> findByTitleEndingWith(String keyword);

	List<Book> findByTitleIn(Collection<String> titles);

	List<Book> findByPublicationYearBetween(int startYear, int endYear);

	List<Book> findByAuthorIsNull();

	List<Book> findByAuthorIsNotNull();

	int deleteByTitleStartingWith(String title);
}
