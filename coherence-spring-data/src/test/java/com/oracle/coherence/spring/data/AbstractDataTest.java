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
package com.oracle.coherence.spring.data;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.oracle.coherence.spring.data.model.Author;
import com.oracle.coherence.spring.data.model.Book;
import com.tangosol.net.NamedMap;
import com.tangosol.util.UUID;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class using {@link Book}s for functional validation.
 */
public abstract class AbstractDataTest {
	/**
	 * Author: Frank Herbert
	 */
	public static final Author FRANK_HERBERT = new Author("Frank", "Herbert");

	/**
	 * Author: J.R.R. (John) Tolkien.
	 */
	public static final Author JOHN_TOLKIEN = new Author("John", "Tolkien");

	/**
	 * Author: Patrick Rothfuss.
	 */
	public static final Author PATRICK_ROTHFUSS = new Author("Patrick", "Rothfuss");

	/**
	 * Author: Stephen King.
	 */
	public static final Author STEPHEN_KING = new Author("Stephen", "King");

	/**
	 * Book: Dune
	 * Author: Frank Herbert
	 */
	public static final Book DUNE = new Book("Dune", 677, FRANK_HERBERT,
			new GregorianCalendar(1964, Calendar.AUGUST, 6, 0, 0));

	/**
	 * Book: Dune Messiah
	 * Author: Frank Herbert
	 */
	public static final Book DUNE_MESSIAH = new Book("Dune Messiah", 468, FRANK_HERBERT,
			new GregorianCalendar(1967, Calendar.JUNE, 6, 0, 0));

	/**
	 * Book: The Name of the Wind
	 * Author: Patrick Rothfuss
	 */
	public static final Book NAME_OF_THE_WIND = new Book("The Name of the Wind", 742, PATRICK_ROTHFUSS,
			new GregorianCalendar(2008, Calendar.MARCH, 6, 0, 0));

	/**
	 * Book: It
	 * Author: Stephen King
	 */
	public static final Book IT = new Book("It", 888, STEPHEN_KING, new GregorianCalendar(1967,
			Calendar.JUNE, 6, 0, 0));

	/**
	 * Book: The Hobbit
	 * Author: John Tolkien
	 */
	public static final Book HOBBIT = new Book("The Hobbit", 355, JOHN_TOLKIEN, new GregorianCalendar(1937,
			Calendar.SEPTEMBER, 21, 0, 0));

	/**
	 * A {@link Set} of {@link Book books} for validating test results.
	 */
	protected Set<Book> books;

	/**
	 * Backing named map for all tests.
	 */
	@Resource(name = "getCache")
	protected NamedMap<UUID, Book> book;

	// ----- test initialization --------------------------------------------

	/**
	 * Initializes/resets the {@link NamedMap} before each test.
	 */
	@BeforeEach
	public void _before() {
		this.book.clear(); // NamedMap

		this.books = new LinkedHashSet<>(4);
		this.books.add(DUNE);
		this.books.add(DUNE_MESSIAH);
		this.books.add(NAME_OF_THE_WIND);
		this.books.add(HOBBIT);

		final Collector<Book, ?, Map<UUID, Book>> collector =
				Collectors.toMap(Book::getUuid, (b) -> b);

		this.book.putAll(this.books.stream().collect(collector));
	}
}
