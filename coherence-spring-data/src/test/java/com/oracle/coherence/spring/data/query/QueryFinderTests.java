/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.data.AbstractDataTest;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.repositories.BookRepository;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(QueryFinderTests.Config.class)
@DirtiesContext
public class QueryFinderTests extends AbstractDataTest {

	@Inject
	protected BookRepository bookRepository;

	@Test
	public void ensureBasicFinderQuery() {
		List<Book> books = this.bookRepository.findByAuthor(FRANK_HERBERT);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	@Test
	public void ensureSimpleIgnoreCase() {
		List<Book> books = this.bookRepository.findByTitleIgnoreCase(DUNE_MESSIAH.getTitle().toLowerCase());
		assertThat(books).isNotNull();
		assertThat(books).containsExactly(DUNE_MESSIAH);
	}

	@Test
	public void ensureGreaterThanEqualFinderQuery() {
		List<Book> books = this.bookRepository.findByPagesGreaterThanEqual(468);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH, NAME_OF_THE_WIND);
	}

	@Test
	void ensureLessThanEqualFinderQuery() {
		List<Book> books = this.bookRepository.findByPagesLessThanEqual(468);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE_MESSIAH, HOBBIT);
	}

	@Test
	public void ensureGreaterThanFinderQuery() {
		List<Book> books = this.bookRepository.findByPagesGreaterThan(468);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, NAME_OF_THE_WIND);
	}

	@Test
	void ensureLessThanFinderQuery() {
		List<Book> books = this.bookRepository.findByPagesLessThan(468);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT);
	}

	@Test
	void ensureLikeFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleLike("%Mess%");
		assertThat(books).isNotNull();
		assertThat(books).containsExactly(DUNE_MESSIAH);
	}

	@Test
	void ensureCaseSensitiveLikeFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleLike("%mess%");
		assertThat(books).isNotNull();
		assertThat(books).isEmpty();
	}

	@Test
	void ensureNotLikeFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleNotLike("%Dune%");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureCaseSensitiveNotLikeFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleNotLike("%dune%");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH, HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureCaseInsensitiveLikeFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleLikeIgnoreCase("%mess%");
		assertThat(books).isNotNull();
		assertThat(books).containsExactly(DUNE_MESSIAH);
	}

	@Test
	void ensureCaseInsensitiveNotLikeFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleNotLikeIgnoreCase("%dune%");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureAfterFinderQuery() {
		List<Book> books = this.bookRepository.findByPublicationYearAfter(1980);
		assertThat(books).isNotNull();
		assertThat(books).containsExactly(NAME_OF_THE_WIND);
	}

	@Test
	void ensureBeforeFinderQuery() {
		List<Book> books = this.bookRepository.findByPublicationYearBefore(1980);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT, DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureContainsFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleContains("Dune");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureStartingWithFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleStartingWith("Dune");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureCaseSensitiveStartingWithFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleStartingWith("dune");
		assertThat(books).isNotNull();
		assertThat(books).isEmpty();
	}

	@Test
	void ensureCaseInsensitiveStartingWithFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleStartingWithIgnoreCase("dune");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureEndingWithFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleEndingWith("Wind");
		assertThat(books).isNotNull();
		assertThat(books).containsOnly(NAME_OF_THE_WIND);
	}

	@Test
	void ensureCaseSensitiveEndingWithFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleEndingWith("wind");
		assertThat(books).isNotNull();
		assertThat(books).isEmpty();
	}

	@Test
	void ensureCaseInsensitiveEndingWithFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleEndingWithIgnoreCase("wind");
		assertThat(books).isNotNull();
		assertThat(books).containsOnly(NAME_OF_THE_WIND);
	}

	@Test
	void ensureInFinderQuery() {
		Collection<String> titles = new ArrayList<>();
		titles.add(DUNE.getTitle());
		titles.add(DUNE_MESSIAH.getTitle());
		List<Book> books = this.bookRepository.findByTitleIn(titles);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureBetweenQuery() {
		List<Book> books = this.bookRepository.findByPublicationYearBetween(1960, 1980);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureNullQuery() {
		List<Book> books = this.bookRepository.findByAuthorIsNull();
		assertThat(books).isNotNull();
		assertThat(books).isEmpty();
	}

	@Test
	void ensureNotNullQuery() {
		List<Book> books = this.bookRepository.findByAuthorIsNotNull();
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH, HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureExistsQuery() {
		assertThat(this.bookRepository.existsByAuthor(FRANK_HERBERT)).isTrue();
	}

	@Test
	void ensureDeleteQuery() {
		int result = this.bookRepository.deleteByTitleStartingWith("Dune");
		assertThat(result).isEqualTo(2);
		assertThat(book.containsKey(DUNE.getUuid())).isFalse();
		assertThat(book.containsKey(DUNE_MESSIAH.getUuid())).isFalse();
		assertThat(book.containsKey(HOBBIT.getUuid())).isTrue();
		assertThat(book.containsKey(NAME_OF_THE_WIND.getUuid())).isTrue();
	}

	@Test
	void ensureOrderedAscQuery() {
		List<Book> books = this.bookRepository.findByAuthorOrderByTitleAsc(FRANK_HERBERT);
		assertThat(books.size()).isEqualTo(2);
		assertThat(books).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureOrderedDescQuery() {
		List<Book> books = this.bookRepository.findByAuthorOrderByTitleDesc(FRANK_HERBERT);
		assertThat(books.size()).isEqualTo(2);
		assertThat(books).containsExactly(DUNE_MESSIAH, DUNE);
	}

	@Test
	void ensureLimitedAscQuery() {
		List<Book> books = this.bookRepository.findTop2ByPagesGreaterThanOrderByTitleAsc(400);
		assertThat(books.size()).isEqualTo(2);
		assertThat(books).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureLimitedDescQuery() {
		List<Book> books = this.bookRepository.findTop2ByPagesGreaterThanOrderByTitleDesc(400);
		assertThat(books.size()).isEqualTo(2);
		assertThat(books).containsExactly(NAME_OF_THE_WIND, DUNE_MESSIAH);
	}

	@Test
	void ensureLimitedCustomSort() {
		List<Book> books = this.bookRepository.findTop3ByPagesGreaterThan(400, Sort.by("title")
				.ascending().and(Sort.by("author").descending()));
		assertThat(books.size()).isEqualTo(3);
		assertThat(books).containsExactly(DUNE, DUNE_MESSIAH, NAME_OF_THE_WIND);
	}

	@Test
	void ensureStreamingQuery() {
		Streamable<Book> stream = this.bookRepository.streamByAuthor(FRANK_HERBERT);
		Collection<Book> books = stream.stream().collect(Collectors.toList());
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensurePageReturnThrows() {
		assertThatThrownBy(() -> this.bookRepository.findByAuthor(FRANK_HERBERT, Pageable.unpaged()))
			.isInstanceOf(UnsupportedOperationException.class)
			.hasMessageContaining("Slice or Page");
	}

	@Test
	void ensureSliceReturnThrows() {
		assertThatThrownBy(() -> this.bookRepository.findByTitle("Dune", Pageable.unpaged()))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("Slice or Page");
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceRepositories("com.oracle.coherence.spring.data.model.repositories")
	public static class Config {
	}
}
