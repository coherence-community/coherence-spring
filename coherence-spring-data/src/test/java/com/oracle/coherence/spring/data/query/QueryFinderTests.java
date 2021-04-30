package com.oracle.coherence.spring.data.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.data.AbstractDataTest;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.repositories.BookRepository;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
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
	void ensureEndingWithFinderQuery() {
		List<Book> books = this.bookRepository.findByTitleEndingWith("Wind");
		assertThat(books).isNotNull();
		assertThat(books).containsExactlyInAnyOrder(NAME_OF_THE_WIND);
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
		System.out.println(books);
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceRepositories("com.oracle.coherence.spring.data.model.repositories")
	public static class Config {
	}
}
