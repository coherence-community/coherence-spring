/*
 * Copyright (c) 2013, 2022, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.data.AbstractDataTest;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.repositories.BookRepository;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

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
	void ensureNotContainsFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleNotContains("Dune");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT, NAME_OF_THE_WIND);
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
	void ensureNotInFinderQuery() {
		Collection<String> titles = new ArrayList<>();
		titles.add(DUNE.getTitle());
		titles.add(DUNE_MESSIAH.getTitle());
		List<Book> books = this.bookRepository.findByTitleNotIn(titles);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT, NAME_OF_THE_WIND);
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
	void ensureMatchesQuery() {
		List<Book> books = this.bookRepository.findByTitleMatches(".+ .+");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE_MESSIAH, HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureIsTrueQuery() {
		List<Book> books = this.bookRepository.findByLongBookIsTrue();
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH, NAME_OF_THE_WIND);
	}

	@Test
	void ensureIsFalseQuery() {
		List<Book> books = this.bookRepository.findByLongBookIsFalse();
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT);
	}

	@Test
	void ensureNotQuery() {
		List<Book> books = this.bookRepository.findByAuthorNot(FRANK_HERBERT);
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureEmptyQuery() {
		List<Book> books = this.bookRepository.findByChaptersEmpty();
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE_MESSIAH, NAME_OF_THE_WIND);
	}

	@Test
	void ensureNotEmptyQuery() {
		List<Book> books = this.bookRepository.findByChaptersNotEmpty();
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(DUNE, HOBBIT);
	}

	@Test
	void ensureSimplePageQueryWithSliceReturn() {
		// first page (1 of 2)
		Pageable pageable = PageRequest.ofSize(1);
		Slice<Book> slice = this.bookRepository.findByTitleStartingWith("Dune", pageable);
		assertThat(slice).isNotNull();
		assertThat(slice.hasNext()).isTrue();
		assertThat(slice.hasPrevious()).isFalse();
		assertThat(slice.hasContent()).isTrue();
		List<Book> content = slice.getContent();
		assertThat(content).isNotNull();
		assertThat(content.size()).isEqualTo(1);
		assertThat(content).containsAnyOf(DUNE, DUNE_MESSIAH);

		// next page (2 of 2)
		pageable = slice.nextPageable();
		slice = this.bookRepository.findByTitleStartingWith("Dune", pageable);
		assertThat(pageable).isNotNull();
		assertThat(slice.hasNext()).isTrue();
		assertThat(slice.hasPrevious()).isTrue();
		assertThat(slice.hasContent()).isTrue();
		List<Book> content2 = slice.getContent();
		assertThat(content2).isNotNull();
		assertThat(content2.size()).isEqualTo(1);
		assertThat(content2).containsAnyOf(DUNE, DUNE_MESSIAH);
		assertThat(content2).isNotEqualTo(content);

		// empty page
		pageable = slice.nextPageable();
		slice = this.bookRepository.findByTitleStartingWith("Dune", pageable);
		assertThat(pageable).isNotNull();
		assertThat(slice.hasNext()).isFalse();
		assertThat(slice.hasPrevious()).isTrue();
		assertThat(slice.hasContent()).isFalse();

		List<Book> content3 = slice.getContent();
		assertThat(content3).isNotNull();
		assertThat(content3.isEmpty()).isTrue();

		// go back one page (2 of 2)
		pageable = slice.previousPageable();
		slice = this.bookRepository.findByTitleStartingWith("Dune", pageable);
		assertThat(pageable).isNotNull();
		assertThat(slice.hasNext()).isTrue();
		assertThat(slice.hasPrevious()).isTrue();
		assertThat(slice.hasContent()).isTrue();
		List<Book> content2prev = slice.getContent();
		assertThat(content2prev).isNotNull();
		assertThat(content2prev.size()).isEqualTo(1);
		assertThat(content2prev).containsAnyOf(DUNE, DUNE_MESSIAH);
		assertThat(content2prev).isEqualTo(content2);
		assertThat(content2prev).isNotEqualTo(content);

		// go back one page (1 of 2)
		pageable = slice.previousPageable();
		slice = this.bookRepository.findByTitleStartingWith("Dune", pageable);
		assertThat(pageable).isNotNull();
		assertThat(slice.hasNext()).isTrue();
		assertThat(slice.hasPrevious()).isFalse();
		assertThat(slice.hasContent()).isTrue();
		List<Book> contentPrev = slice.getContent();
		assertThat(contentPrev).isNotNull();
		assertThat(contentPrev.size()).isEqualTo(1);
		assertThat(contentPrev).containsAnyOf(DUNE, DUNE_MESSIAH);
		assertThat(contentPrev).isEqualTo(content);
		assertThat(contentPrev).isNotEqualTo(content2);
	}

	@Test
	void ensureSimplePageQueryWithPageReturn() {
		// first page (1 of 2)
		Pageable pageable = PageRequest.ofSize(1);
		Page<Book> page = this.bookRepository.findByAuthor(FRANK_HERBERT, pageable);
		assertThat(page).isNotNull();
		assertThat(page.hasNext()).isTrue();
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasContent()).isTrue();
		assertThat(page.getTotalPages()).isEqualTo(2);
		assertThat(page.getTotalElements()).isEqualTo(2);
		List<Book> content = page.getContent();
		assertThat(content).isNotNull();
		assertThat(content.size()).isEqualTo(1);
		assertThat(content).containsAnyOf(DUNE, DUNE_MESSIAH);

		// next page (2 of 2)
		pageable = page.nextPageable();
		page = this.bookRepository.findByAuthor(FRANK_HERBERT, pageable);
		assertThat(pageable).isNotNull();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.hasPrevious()).isTrue();
		assertThat(page.hasContent()).isTrue();
		List<Book> content2 = page.getContent();
		assertThat(content2).isNotNull();
		assertThat(content2.size()).isEqualTo(1);
		assertThat(content2).containsAnyOf(DUNE, DUNE_MESSIAH);
		assertThat(content2).isNotEqualTo(content);

		// go back one page (1 of 2)
		pageable = page.previousPageable();
		page =  this.bookRepository.findByAuthor(FRANK_HERBERT, pageable);
		assertThat(pageable).isNotNull();
		assertThat(page.hasNext()).isTrue();
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasContent()).isTrue();
		List<Book> contentPrev = page.getContent();
		assertThat(contentPrev).isNotNull();
		assertThat(contentPrev.size()).isEqualTo(1);
		assertThat(contentPrev).containsAnyOf(DUNE, DUNE_MESSIAH);
		assertThat(contentPrev).isEqualTo(content);
		assertThat(contentPrev).isNotEqualTo(content2);
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceRepositories("com.oracle.coherence.spring.data.model.repositories")
	public static class Config {
	}
}
