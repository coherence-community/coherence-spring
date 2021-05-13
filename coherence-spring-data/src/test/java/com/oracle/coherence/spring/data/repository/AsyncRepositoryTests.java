/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.data.AbstractDataTest;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.model.Author;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.repositories.CoherenceBookAsyncRepository;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.Fragment;
import com.tangosol.util.UUID;
import com.tangosol.util.function.Remote;
import com.tangosol.util.stream.RemoteCollectors;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.tangosol.util.Extractors.fragment;
import static com.tangosol.util.Filters.equal;
import static com.tangosol.util.Filters.greater;
import static com.tangosol.util.function.Remote.comparator;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

@SpringJUnitConfig(AsyncRepositoryTests.Config.class)
@DirtiesContext
public class AsyncRepositoryTests extends AbstractDataTest {

	@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection", "CdiInjectionPointsInspection"})
	@Inject
	protected CoherenceBookAsyncRepository bookRepository;

	@Test
	public void ensureDeleteAll() throws Exception {
		this.bookRepository.deleteAll().get(5, TimeUnit.SECONDS);
		assertThat(this.book.isEmpty()).isTrue();
	}

	@Test
	public void ensureSave() throws Exception {
		assertThat(this.bookRepository.save(IT).get(5, TimeUnit.SECONDS)).isEqualTo(IT);
	}

	@Test
	public void ensureSaveAllWithIterable() throws Exception {
		this.bookRepository.deleteAll().join(); // fresh start
		Iterable<Book> result = this.bookRepository.saveAll(this.books).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAll(this.books);
	}

	@Test
	public void ensureFindById() throws Exception {
		assertThat(this.bookRepository.findById(DUNE.getUuid()).get(5, TimeUnit.SECONDS)).isEqualTo(Optional.of(DUNE));
	}

	@Test
	public void ensureFindByIdNotFound() throws Exception {
		assertThat(this.bookRepository.findById(new UUID()).get(5, TimeUnit.SECONDS)).isEqualTo(Optional.empty());
	}

	@Test
	void ensureExistsById() throws Exception {
		assertThat(this.bookRepository.existsById(DUNE.getUuid()).get(5, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void ensureExistsByIdNotFound() throws Exception {
		assertThat(this.bookRepository.existsById(new UUID()).get(5, TimeUnit.SECONDS)).isFalse();
	}

	@Test
	void ensureFindAll() throws Exception {
		assertThat(this.bookRepository.findAll().get(5, TimeUnit.SECONDS)).containsExactlyInAnyOrderElementsOf(this.books);
	}

	@Test
	void ensureFindAllById() throws Exception {
		assertThat(this.bookRepository.findAllById(asList(DUNE.getUuid(),
				NAME_OF_THE_WIND.getUuid())).get(5, TimeUnit.SECONDS)).containsExactlyInAnyOrder(DUNE, NAME_OF_THE_WIND);
	}

	@Test
	void ensureCount() throws Exception {
		assertThat(this.bookRepository.count().get(5, TimeUnit.SECONDS)).isEqualTo(4);
	}

	@Test
	void ensureDeleteById() throws Exception {
		assertThat(this.book.containsKey(DUNE.getUuid())).isTrue();
		this.bookRepository.deleteById(DUNE.getUuid()).get(5, TimeUnit.SECONDS);
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureDelete() throws Exception {
		assertThat(this.book.containsKey(DUNE.getUuid())).isTrue();
		this.bookRepository.delete(DUNE).get(5, TimeUnit.SECONDS);
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureDeleteAllWithIterable() throws Exception {
		assertThat(this.book.isEmpty()).isFalse();
		this.bookRepository.deleteAll((Iterable<? extends Book>) books).get(5, TimeUnit.SECONDS);
		assertThat(this.book.isEmpty()).isTrue();
	}

	@Test
	void ensureFindAllWithFilter() throws Exception {
		assertThat(this.bookRepository.getAll(author()).get(5, TimeUnit.SECONDS))
				.containsExactlyInAnyOrderElementsOf(
						this.books.stream().filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
								.collect(toSet()));
	}

	@Test
	void ensureFindAllOrdered() throws Exception {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(Book::getTitle).get(5, TimeUnit.SECONDS);
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH, HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureFindAllWithFilterOrdered() throws Exception {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(author(), Book::getTitle).get(5, TimeUnit.SECONDS);
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureFindAllWithRemoteComparator() throws Exception {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(comparator((Remote.Comparator<Book>) (o1, o2) -> {
			int c1 = o1.getAuthor().compareTo(o2.getAuthor());
			if (c1 == 0) {
				return o1.getPublished().compareTo(o2.getPublished());
			}
			return c1;
		})).get(5, TimeUnit.SECONDS);
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH, NAME_OF_THE_WIND, HOBBIT);
	}

	@Test
	void ensureFindAllWithFilterAndRemoteComparator() throws Exception {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(author(),
				comparator((Remote.Comparator<Book>) (o1, o2) -> {
			int c1 = o1.getAuthor().compareTo(o2.getAuthor());
			if (c1 == 0) {
				return o1.getPublished().compareTo(o2.getPublished());
			}
			return c1;
		})).get(5, TimeUnit.SECONDS);
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	public void ensureSaveAllWithStream() throws Exception {
		this.bookRepository.deleteAll().get(5, TimeUnit.SECONDS); // fresh start
		assertThat(this.book.size()).isEqualTo(0);
		this.bookRepository.saveAll(this.books.stream()).get(5, TimeUnit.SECONDS);
		assertThat(this.book.size()).isEqualTo(4);
	}

	@Test
	public void ensureGetWithIdAndValueExtractor() throws Exception {
		Author author = this.bookRepository.get(DUNE.getUuid(), Book::getAuthor).get(5, TimeUnit.SECONDS);
		assertThat(author).isEqualTo(DUNE.getAuthor());
	}

	@Test
	public void ensureGetWithFragment() throws Exception {
		Fragment<Author> authorFragment = this.bookRepository.get(DUNE.getUuid(),
				fragment(Book::getAuthor, Author::getFirstName, Author::getLastName)).get(5, TimeUnit.SECONDS);
		String firstName = authorFragment.get("firstName");
		String lastName = authorFragment.get("lastName");
		assertThat(firstName).isEqualTo(FRANK_HERBERT.getFirstName());
		assertThat(lastName).isEqualTo(FRANK_HERBERT.getLastName());
	}

	@Test
	public void ensureGetAllWithExtractor() throws Exception {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(fragment(Book::getAuthor, Author::getFirstName)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(
				this.books.stream().collect(
						toMap(Book::getUuid, (bk) -> {
							Map<String, Object> fragMap = new HashMap<>();
							fragMap.put("firstName", bk.getAuthor().getFirstName());
							return new Fragment<>(fragMap);
						})));
	}

	@Test
	public void ensureGetAllWithIdAndExtractor() throws Exception {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()), fragment(Book::getAuthor, Author::getFirstName)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(
				this.books.stream()
						.filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
						.collect(Collectors.toMap(Book::getUuid, (bk) -> {
							Map<String, Object> fragMap = new HashMap<>();
							fragMap.put("firstName", bk.getAuthor().getFirstName());
							return new Fragment<>(fragMap);
						})));
	}

	@Test
	public void ensureGetAllWithFilterAndExtractor() throws Exception {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				author(), fragment(Book::getAuthor, Author::getFirstName)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(
				this.books.stream()
						.filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
						.collect(Collectors.toMap(Book::getUuid, (bk) -> {
							Map<String, Object> fragMap = new HashMap<>();
							fragMap.put("firstName", bk.getAuthor().getFirstName());
							return new Fragment<>(fragMap);
						})));
	}

	@Test
	void ensureGetAllWithFragment() throws Exception {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				fragment(Book::getAuthor, Author::getFirstName, Author::getLastName)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(
				this.books.stream().collect(
						Collectors.toMap(Book::getUuid, (bk) -> {
							Map<String, Object> fragment = new HashMap<>();
							fragment.put("firstName", bk.getAuthor().getFirstName());
							fragment.put("lastName", bk.getAuthor().getLastName());
							return new Fragment<>(fragment);
						})));
	}

	@Test
	void ensureGetAllWithIdsAndFragment() throws Exception {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()),
				fragment(Book::getAuthor, Author::getFirstName, Author::getLastName)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(
				this.books.stream()
						.filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
						.collect(
						Collectors.toMap(Book::getUuid, (bk) -> {
							Map<String, Object> fragment = new HashMap<>();
							fragment.put("firstName", bk.getAuthor().getFirstName());
							fragment.put("lastName", bk.getAuthor().getLastName());
							return new Fragment<>(fragment);
						})));
	}

	@Test
	void ensureGetAllWithFilterAndFragment() throws Exception {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				author(), fragment(Book::getAuthor, Author::getFirstName, Author::getLastName)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(
				this.books.stream()
						.filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
						.collect(
								Collectors.toMap(Book::getUuid, (bk) -> {
									Map<String, Object> fragment = new HashMap<>(1, 1.0f);
									fragment.put("firstName", bk.getAuthor().getFirstName());
									fragment.put("lastName", bk.getAuthor().getLastName());
									return new Fragment<>(fragment);
								})));
	}

	@Test
	void ensureUpdate() throws Exception {
		this.bookRepository.update(DUNE.getUuid(), Book::setPages, 700).get(5, TimeUnit.SECONDS);
		assertThat(this.book.get(DUNE.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithEntityFactory() throws Exception {
		this.bookRepository.update(IT.getUuid(), Book::setPages, 700, Book::new).get(5, TimeUnit.SECONDS);
		assertThat(this.book.get(IT.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdAndRemoteFunction() throws Exception {
		int result = this.bookRepository.update(DUNE.getUuid(), Remote.function((bk) -> {
			bk.setPages(700);
			return 700;
		})).get(5, TimeUnit.SECONDS);
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(DUNE.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdRemoteFunctionAndEntityFactory() throws Exception {
		int result = this.bookRepository.update(IT.getUuid(), Remote.function((bk) -> {
			bk.setPages(700);
			return 700;
		}), Book::new).get(5, TimeUnit.SECONDS);
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(IT.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdAndRemoteBiFunction() throws Exception {
		Remote.BiFunction<Book, Integer, Integer> function = (bk, pages) -> {
			bk.setPages(pages);
			return pages;
		};
		int result = this.bookRepository.update(DUNE.getUuid(), function, 700).get(5, TimeUnit.SECONDS);
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(DUNE.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdRemoteBiFunctionAndEntityFactory() throws Exception {
		Remote.BiFunction<Book, Integer, Integer> function = (bk, pages) -> {
			bk.setPages(pages);
			return pages;
		};
		int result = this.bookRepository.update(IT.getUuid(), function, 700, Book::new).get(5, TimeUnit.SECONDS);
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(IT.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateAllWithFilter() throws Exception {
		this.bookRepository.updateAll(author(), Book::setPages, 700).get(5, TimeUnit.SECONDS);
		this.book.forEach((id, bk) -> {
			if (bk.getAuthor().equals(FRANK_HERBERT)) {
				assertThat(bk.getPages()).isEqualTo(700);
			}
		});

		assertThat(this.book.get(NAME_OF_THE_WIND.getUuid()).getPages()).isEqualTo(NAME_OF_THE_WIND.getPages());
		assertThat(this.book.get(HOBBIT.getUuid()).getPages()).isEqualTo(HOBBIT.getPages());
	}

	@Test
	void ensureUpdateAllWithFilterAndRemoteFunction() throws Exception {
		Map<UUID, Integer> result = this.bookRepository.updateAll(author(), Remote.function((bk) -> {
			bk.setPages(700);
			return 700;
		})).get(5, TimeUnit.SECONDS);

		Map<UUID, Integer> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), 700);
		expected.put(DUNE_MESSIAH.getUuid(), 700);
		assertThat(result).containsAllEntriesOf(expected);

		this.book.forEach((id, bk) -> {
			if (bk.getAuthor().equals(FRANK_HERBERT)) {
				assertThat(bk.getPages()).isEqualTo(700);
			}
		});

		assertThat(this.book.get(NAME_OF_THE_WIND.getUuid()).getPages()).isEqualTo(NAME_OF_THE_WIND.getPages());
		assertThat(this.book.get(HOBBIT.getUuid()).getPages()).isEqualTo(HOBBIT.getPages());
	}

	@Test
	void ensureUpdateAllWithFilterAndRemoteBiFunction() throws Exception {
		Remote.BiFunction<Book, Integer, Integer> function = (bk, pages) -> {
			bk.setPages(pages);
			return pages;
		};
		Map<UUID, Integer> result = this.bookRepository.updateAll(author(), function, 700).get(5, TimeUnit.SECONDS);

		Map<UUID, Integer> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), 700);
		expected.put(DUNE_MESSIAH.getUuid(), 700);
		assertThat(result).containsAllEntriesOf(expected);

		this.book.forEach((id, bk) -> {
			if (bk.getAuthor().equals(FRANK_HERBERT)) {
				assertThat(bk.getPages()).isEqualTo(700);
			}
		});

		assertThat(this.book.get(NAME_OF_THE_WIND.getUuid()).getPages()).isEqualTo(NAME_OF_THE_WIND.getPages());
		assertThat(this.book.get(HOBBIT.getUuid()).getPages()).isEqualTo(HOBBIT.getPages());
	}

	@Test
	void ensureRemoteWithReturn() throws Exception {
		assertThat(this.bookRepository.delete(DUNE, true).get(5, TimeUnit.SECONDS)).isEqualTo(DUNE);
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureRemoteWithNoReturn() throws Exception {
		assertThat(this.bookRepository.delete(DUNE, false).get(5, TimeUnit.SECONDS)).isNull();
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureDeleteAllByIdWithIterable() {
		Iterable<UUID> iterable = Arrays.asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid());
		this.bookRepository.deleteAllById(iterable).join();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllById() throws Exception {
		boolean result = this.bookRepository.deleteAllById(asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid())).get(5, TimeUnit.SECONDS);
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllByIdNoMatch() throws Exception {
		boolean result = this.bookRepository.deleteAllById(singletonList(IT.getUuid())).get(5, TimeUnit.SECONDS);
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllByIdWithReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAllById(asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()), true).get(5, TimeUnit.SECONDS);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllByIdWithNoReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAllById(asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()), false).get(5, TimeUnit.SECONDS);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithCollection() throws Exception {
		boolean result = this.bookRepository.deleteAll(asList(DUNE, DUNE_MESSIAH)).get(5, TimeUnit.SECONDS);
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithCollectionNoMatch() throws Exception {
		boolean result = this.bookRepository.deleteAll(singletonList(IT)).get(5, TimeUnit.SECONDS);
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithCollectionAndReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAll(asList(DUNE, DUNE_MESSIAH), true).get(5, TimeUnit.SECONDS);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithCollectionAndNoReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAll(Collections.singletonList(IT), false).get(5, TimeUnit.SECONDS);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithStream() throws Exception {
		boolean result = this.bookRepository.deleteAll(Stream.of(DUNE, DUNE_MESSIAH)).get(5, TimeUnit.SECONDS);
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithStreamNoMatch() throws Exception {
		boolean result = this.bookRepository.deleteAll(Stream.of(IT)).get(5, TimeUnit.SECONDS);
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithStreamAndReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAll(Stream.of(DUNE, DUNE_MESSIAH), true).get(5, TimeUnit.SECONDS);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithStreamAndNoReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAll(Stream.of(DUNE, DUNE_MESSIAH), false).get(5, TimeUnit.SECONDS);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithFilter() throws Exception {
		boolean result = this.bookRepository.deleteAll(author()).get(5, TimeUnit.SECONDS);
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithFilterNoMatch() throws Exception {
		boolean result = this.bookRepository.deleteAll(equal("author", STEPHEN_KING)).get(5, TimeUnit.SECONDS);
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithFilterAndReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAll(author(), true).get(5, TimeUnit.SECONDS);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithFilterAndNoReturn() throws Exception {
		Map<UUID, Book> result = this.bookRepository.deleteAll(author(), false).get(5, TimeUnit.SECONDS);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}


	@Test
	void ensureCountWithFilter() throws Exception {
		assertThat(this.bookRepository.count(author()).get(5, TimeUnit.SECONDS)).isEqualTo(2L);
	}

	@Test
	void ensureMax() throws Exception {
		assertThat(this.bookRepository.max(Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(NAME_OF_THE_WIND.getPages());
	}

	@Test
	void ensureMaxWithFilter() throws Exception {
		assertThat(this.bookRepository.max(author(),
				Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(DUNE.getPages());
	}

	@Test
	void ensureMaxLong() throws Exception {
		assertThat(this.bookRepository.max(Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(NAME_OF_THE_WIND.getPagesAsLong());
	}

	@Test
	void ensureMaxLongWithFilter() throws Exception {
		assertThat(this.bookRepository.max(author(), Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(DUNE.getPagesAsLong());
	}

	@Test
	void ensureMaxDouble() throws Exception {
		assertThat(this.bookRepository.max(Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(NAME_OF_THE_WIND.getPagesAsDouble());
	}

	@Test
	void ensureMaxDoubleWithFilter() throws Exception {
		assertThat(this.bookRepository.max(author(), Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(DUNE.getPagesAsDouble());
	}

	@Test
	void ensureMaxBigDecimal() throws Exception {
		assertThat(this.bookRepository.max(Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isEqualTo(NAME_OF_THE_WIND.getPagesAsBigDecimal());
	}

	@Test
	void ensureMaxBigDecimalWithFilter() throws Exception {
		assertThat(this.bookRepository.max(author(), Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isEqualTo(DUNE.getPagesAsBigDecimal());
	}

	@Test
	void ensureMaxWithRemoteCompareFunction() throws Exception {
		assertThat(this.bookRepository.max(
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear).get(5, TimeUnit.SECONDS))
				.isEqualTo(2008);
	}

	@Test
	void ensureMaxWithFilterAndRemoteCompareFunction() throws Exception {
		assertThat(this.bookRepository.max(author(),
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear).get(5, TimeUnit.SECONDS))
				.isEqualTo(1967);
	}

	@Test
	void ensureMaxBy() throws Exception {
		assertThat(this.bookRepository.maxBy(Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(Optional.of(NAME_OF_THE_WIND));
	}

	@Test
	void ensureMaxByWithFilter() throws Exception {
		assertThat(this.bookRepository.maxBy(author(), Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(Optional.of(DUNE));
	}

	@Test
	void ensureMin() throws Exception {
		assertThat(this.bookRepository.min(Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(HOBBIT.getPages());
	}

	@Test
	void ensureMinWithFilter() throws Exception {
		assertThat(this.bookRepository.min(author(),
				Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(DUNE_MESSIAH.getPages());
	}

	@Test
	void ensureMinLong() throws Exception {
		assertThat(this.bookRepository.min(Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(HOBBIT.getPagesAsLong());
	}

	@Test
	void ensureMinLongWithFilter() throws Exception {
		assertThat(this.bookRepository.min(author(), Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(DUNE_MESSIAH.getPagesAsLong());
	}

	@Test
	void ensureMinDouble() throws Exception {
		assertThat(this.bookRepository.min(Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(HOBBIT.getPagesAsDouble());
	}

	@Test
	void ensureMinDoubleWithFilter() throws Exception {
		assertThat(this.bookRepository.min(author(), Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(DUNE_MESSIAH.getPagesAsDouble());
	}

	@Test
	void ensureMinBigDecimal() throws Exception {
		assertThat(this.bookRepository.min(Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isEqualTo(HOBBIT.getPagesAsBigDecimal());
	}

	@Test
	void ensureMinBigDecimalWithFilter() throws Exception {
		assertThat(this.bookRepository.min(author(), Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isEqualTo(DUNE_MESSIAH.getPagesAsBigDecimal());
	}

	@Test
	void ensureMinWithRemoteCompareFunction() throws Exception {
		assertThat(this.bookRepository.min(
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear).get(5, TimeUnit.SECONDS))
				.isEqualTo(1937);
	}

	@Test
	void ensureMinWithFilterAndRemoteCompareFunction() throws Exception {
		assertThat(this.bookRepository.min(author(),
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear).get(5, TimeUnit.SECONDS))
				.isEqualTo(1964);
	}

	@Test
	void ensureMinBy() throws Exception {
		assertThat(this.bookRepository.minBy(Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(Optional.of(HOBBIT));
	}

	@Test
	void ensureMinByWithFilter() throws Exception {
		assertThat(this.bookRepository.minBy(author(), Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(Optional.of(DUNE_MESSIAH));
	}

	@Test
	void ensureSum() throws Exception {
		assertThat(this.bookRepository.sum(Book::getPages).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().mapToLong(Book::getPages).sum());
	}

	@Test
	void ensureSumWithFilter() throws Exception {
		assertThat(this.bookRepository.sum(author(),
				Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(books.stream().filter((bk) ->
					bk.getAuthor().equals(FRANK_HERBERT)).mapToInt(Book::getPages).sum());
	}

	@Test
	void ensureSumLong() throws Exception {
		assertThat(this.bookRepository.sum(Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().mapToLong(Book::getPagesAsLong).sum());
	}

	@Test
	void ensureSumLongWithFilter() throws Exception {
		assertThat(this.bookRepository.sum(author(), Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPagesAsLong).sum());
	}

	@Test
	void ensureSumDouble() throws Exception {
		assertThat(this.bookRepository.sum(Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().mapToDouble(Book::getPagesAsDouble).sum());
	}

	@Test
	void ensureSumDoubleWithFilter() throws Exception {
		assertThat(this.bookRepository.sum(author(), Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToDouble(Book::getPagesAsDouble).sum());
	}

	@Test
	void ensureSumBigDecimal() throws Exception {
		assertThat(this.bookRepository.sum(Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isEqualTo(BigDecimal.valueOf(books.stream().mapToLong(Book::getPagesAsLong).sum()));
	}

	@Test
	void ensureSumBigDecimalWithFilter() throws Exception {
		assertThat(this.bookRepository.sum(author(), Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isEqualTo(BigDecimal.valueOf(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPages).sum()));
	}

	@Test
	void ensureAverage() throws Exception {
		assertThat(this.bookRepository.average(Book::getPages).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().mapToInt(Book::getPages).average().orElse(Double.NEGATIVE_INFINITY));
	}

	@Test
	void ensureAverageWithFilter() throws Exception {
		assertThat(this.bookRepository.average(author(),
				Book::getPages).get(5, TimeUnit.SECONDS)).isEqualTo(books.stream().filter((bk) ->
				bk.getAuthor().equals(FRANK_HERBERT)).mapToInt(Book::getPages).average().orElse(Double.NEGATIVE_INFINITY));
	}

	@Test
	void ensureAverageLong() throws Exception {
		assertThat(this.bookRepository.average(Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().mapToLong(Book::getPagesAsLong).average().orElse(Double.NEGATIVE_INFINITY));
	}

	@Test
	void ensureAverageLongWithFilter() throws Exception {
		assertThat(this.bookRepository.average(author(), Book::getPagesAsLong).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPagesAsLong).average().orElse(Double.NEGATIVE_INFINITY));
	}

	@Test
	void ensureAverageDouble() throws Exception {
		assertThat(this.bookRepository.average(Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().mapToDouble(Book::getPagesAsDouble).average().orElse(Double.NEGATIVE_INFINITY));
	}

	@Test
	void ensureAverageDoubleWithFilter() throws Exception {
		assertThat(this.bookRepository.average(author(), Book::getPagesAsDouble).get(5, TimeUnit.SECONDS))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToDouble(Book::getPagesAsDouble).average().orElse(Double.NEGATIVE_INFINITY));
	}

	@Test
	void ensureAverageBigDecimal() throws Exception {
		assertThat(this.bookRepository.average(Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isCloseTo(BigDecimal.valueOf(books.stream().mapToLong(Book::getPagesAsLong).average().orElse(Double.NEGATIVE_INFINITY)), withPercentage(.1));
	}

	@Test
	void ensureAverageBigDecimalWithFilter() throws Exception {
		assertThat(this.bookRepository.average(author(), Book::getPagesAsBigDecimal).get(5, TimeUnit.SECONDS))
				.isCloseTo(BigDecimal.valueOf(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPages).average().orElse(Double.NEGATIVE_INFINITY)), withPercentage(.1));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	void ensureDistinct() throws Exception {
		// intentional lack of typing on list
		List authors = asList(FRANK_HERBERT, JOHN_TOLKIEN, PATRICK_ROTHFUSS);
		assertThat(this.bookRepository.distinct(Book::getAuthor).get(5, TimeUnit.SECONDS))
				.containsExactlyInAnyOrderElementsOf(authors);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	void ensureDistinctFilter() throws Exception {
		// intentional lack of typing on list
		List authors = asList(FRANK_HERBERT, JOHN_TOLKIEN, PATRICK_ROTHFUSS);
		assertThat(this.bookRepository.distinct(greater(Book::getPages, 100), Book::getAuthor).get(5, TimeUnit.SECONDS))
				.containsExactlyInAnyOrderElementsOf(authors);
	}

	@Test
	void ensureGroupBy() throws Exception {
		Map<Author, Set<Book>> result = this.bookRepository.groupBy(Book::getAuthor).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, toSet())));
	}

	@Test
	void ensureGroupByWithSort() throws Exception {
		Map<Author, SortedSet<Book>> result = this.bookRepository.groupBy(Book::getAuthor,
				Remote.comparator((Remote.Comparator<Book>) (o1, o2) -> o1.getTitle().compareTo(o2.getTitle()))).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.toCollection(() -> (SortedSet<Book>) new TreeSet<>(Comparator.comparing(Book::getTitle))))));
		assertThat(result.get(FRANK_HERBERT)).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureGroupByWithFilter() throws Exception {
		Map<Author, Set<Book>> result = this.bookRepository.groupBy(greater(Book::getPages, 500), Book::getAuthor).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getPages() > 500)
				.collect(Collectors.groupingBy(Book::getAuthor, toSet())));
	}

	@Test
	void ensureGroupByWithFilterAndSort() throws Exception {
		Map<Author, SortedSet<Book>> result = this.bookRepository.groupBy(greater(Book::getPages, 500), Book::getAuthor,
				Remote.comparator((Remote.Comparator<Book>) (o1, o2) -> o1.getTitle().compareTo(o2.getTitle()))).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getPages() > 500)
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.toCollection(() -> (SortedSet<Book>) new TreeSet<>(Comparator.comparing(Book::getTitle))))));
		assertThat(result.get(FRANK_HERBERT)).containsExactly(DUNE);
	}

	@Test
	void ensureGroupByWithRemoteCollector() throws Exception {
		Map<Author, Integer> result = this.bookRepository.groupBy(Book::getAuthor, RemoteCollectors.summingInt(Book::getPages)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureGroupByWithFilterAndRemoteCollector() throws Exception {
		Map<Author, Integer> result = this.bookRepository.groupBy(author(), Book::getAuthor, RemoteCollectors.summingInt(Book::getPages)).get(5, TimeUnit.SECONDS);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
				.collect(Collectors.groupingBy(Book::getAuthor,  Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureGroupByWithRemoteCollectorAndMapFactory() throws Exception {
		Map<Author, Integer> result = this.bookRepository.groupBy(Book::getAuthor, Remote.supplier(TreeMap::new), RemoteCollectors.summingInt(Book::getPages)).get(5, TimeUnit.SECONDS);
		assertThat(result).isInstanceOf(TreeMap.class);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureGroupByWithFilterRemoteCollectorAndMapFactory() throws Exception {
		Map<Author, Integer> result = this.bookRepository.groupBy(Filters.greater(Book::getPages, 500),
				Book::getAuthor, Remote.supplier(TreeMap::new),
				RemoteCollectors.summingInt(Book::getPages)).get(5, TimeUnit.SECONDS);
		assertThat(result).isInstanceOf(TreeMap.class);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getPages() > 500)
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureTop() throws Exception {
		List<Integer> topPages = this.bookRepository.top(Book::getPages, 3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(742, 677, 468);
	}

	@Test
	void ensureTopWithFilter() throws Exception {
		List<Integer> topPages = this.bookRepository.top(author(), Book::getPages, 3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(677, 468);
	}

	@Test
	void ensureTopWithRemoteComparator() throws Exception {
		List<Integer> topPages = this.bookRepository.top(Book::getPages,
				Remote.Comparator.reverseOrder(),
				3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(355, 468, 677);
	}

	@Test
	void ensureTopWithFilterAndRemoteComparator() throws Exception {
		List<Integer> topPages = this.bookRepository.top(author(), Book::getPages,
				Remote.Comparator.reverseOrder(),
				3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(468, 677);
	}

	@Test
	void ensureTopBy() throws Exception {
		List<Book> topPages = this.bookRepository.topBy(Book::getPages, 3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(NAME_OF_THE_WIND, DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureTopByFilter() throws Exception {
		List<Book> topPages = this.bookRepository.topBy(author(), Book::getPages, 3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureTopByWithRemoteComparator() throws Exception {
		List<Book> topPages = this.bookRepository.topBy(Remote.Comparator.comparing(Book::getTitle), 3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(NAME_OF_THE_WIND, HOBBIT, DUNE_MESSIAH);
	}

	@Test
	void ensureTopByWithFilterAndRemoteComparator() throws Exception {
		List<Book> topPages = this.bookRepository.topBy(Filters.greater(Book::getPages, 500), Remote.Comparator.comparing(Book::getTitle), 3).get(5, TimeUnit.SECONDS);
		assertThat(topPages).containsExactly(NAME_OF_THE_WIND, DUNE);
	}

	@Test
	void ensureFinderQueries() throws Exception {
		List<Book> books = this.bookRepository.findByAuthor(FRANK_HERBERT).get(5, TimeUnit.SECONDS);
		assertThat(books).containsExactlyInAnyOrder(DUNE, DUNE_MESSIAH);
	}

	// ----- helper methods -------------------------------------------------

	Filter<Author> author() {
		return equal("author", AbstractDataTest.FRANK_HERBERT);
	}

	@Configuration
	@EnableAsync
	@EnableCoherence
	@EnableCoherenceRepositories("com.oracle.coherence.spring.data.model.repositories")
	public static class Config {
	}
}
