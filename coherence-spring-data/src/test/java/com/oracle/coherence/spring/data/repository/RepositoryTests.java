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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.data.AbstractDataTest;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.model.Author;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.repositories.CoherenceBookRepository;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.Fragment;
import com.tangosol.util.UUID;
import com.tangosol.util.function.Remote;
import com.tangosol.util.stream.RemoteCollectors;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.tangosol.util.Extractors.extract;
import static com.tangosol.util.Extractors.fragment;
import static com.tangosol.util.Filters.equal;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(RepositoryTests.Config.class)
@DirtiesContext
public class RepositoryTests extends AbstractDataTest {

	@Inject
	protected CoherenceBookRepository bookRepository;

	@Test
	public void ensureDeleteAll() {
		this.bookRepository.deleteAll();
		assertThat(this.book.isEmpty()).isTrue();
	}

	@Test
	public void ensureSave() {
		assertThat(this.bookRepository.save(IT)).isEqualTo(IT);
	}

	@Test
	public void ensureSaveAllWithIterable() {
		this.bookRepository.deleteAll(); // fresh start
		Iterable<Book> result = this.bookRepository.saveAll(this.books);
		assertThat(result).containsAll(this.books);
	}

	@Test
	public void ensureFindById() {
		assertThat(this.bookRepository.findById(DUNE.getUuid())).isEqualTo(Optional.of(DUNE));
	}

	@Test
	public void ensureFindByIdNotFound() {
		assertThat(this.bookRepository.findById(new UUID())).isEqualTo(Optional.empty());
	}

	@Test
	void ensureExistsById() {
		assertThat(this.bookRepository.existsById(DUNE.getUuid())).isTrue();
	}

	@Test
	void ensureExistsByIdNotFound() {
		assertThat(this.bookRepository.existsById(new UUID())).isFalse();
	}

	@Test
	void ensureFindAll() {
		assertThat(this.bookRepository.findAll()).containsExactlyInAnyOrderElementsOf(this.books);
	}

	@Test
	void ensureFindAllById() {
		assertThat(this.bookRepository.findAllById(Arrays.asList(DUNE.getUuid(),
				NAME_OF_THE_WIND.getUuid()))).containsExactlyInAnyOrder(DUNE, NAME_OF_THE_WIND);
	}

	@Test
	void ensureCount() {
		assertThat(this.bookRepository.count()).isEqualTo(4);
	}

	@Test
	void ensureDeleteById() {
		assertThat(this.book.containsKey(DUNE.getUuid())).isTrue();
		this.bookRepository.deleteById(DUNE.getUuid());
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureDelete() {
		assertThat(this.book.containsKey(DUNE.getUuid())).isTrue();
		this.bookRepository.delete(DUNE);
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureDeleteAllWithIterable() {
		assertThat(this.book.isEmpty()).isFalse();
		this.bookRepository.deleteAll((Iterable<? extends Book>) books);
		assertThat(this.book.isEmpty()).isTrue();
	}

	@Test
	void ensureFindAllWithFilter() {
		assertThat(this.bookRepository.getAll(author()))
				.containsExactlyInAnyOrderElementsOf(
						this.books.stream().filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
								.collect(toSet()));
	}

	@Test
	void ensureFindAllOrdered() {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(extract("title"));
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH, HOBBIT, NAME_OF_THE_WIND);
	}

	@Test
	void ensureFindAllWithFilterOrdered() {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(author(), extract("title"));
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureFindAllWithRemoteComparator() {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(Remote.comparator((Remote.Comparator<Book>) (o1, o2) -> {
			int c1 = o1.getAuthor().compareTo(o2.getAuthor());
			if (c1 == 0) {
				return o1.getPublished().compareTo(o2.getPublished());
			}
			return c1;
		}));
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH, NAME_OF_THE_WIND, HOBBIT);
	}

	@Test
	void ensureFindAllWithFilterAndRemoteComparator() {
		Collection<Book> result = this.bookRepository.getAllOrderedBy(author(),
				Remote.comparator((Remote.Comparator<Book>) (o1, o2) -> {
			int c1 = o1.getAuthor().compareTo(o2.getAuthor());
			if (c1 == 0) {
				return o1.getPublished().compareTo(o2.getPublished());
			}
			return c1;
		}));
		assertThat(result).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	public void ensureSaveAllWithStream() {
		this.bookRepository.deleteAll(); // fresh start
		assertThat(this.book.size()).isEqualTo(0);
		this.bookRepository.saveAll(this.books.stream());
		assertThat(this.book.size()).isEqualTo(4);
	}

	@Test
	public void ensureGetWithIdAndValueExtractor() {
		Author author = this.bookRepository.get(DUNE.getUuid(), extract("author"));
		assertThat(author).isEqualTo(DUNE.getAuthor());
	}

	@Test
	public void ensureGetWithFragment() {
		Fragment<Author> authorFragment = this.bookRepository.get(DUNE.getUuid(),
				fragment(Book::getAuthor, Author::getFirstName, Author::getLastName));
		String firstName = authorFragment.get("firstName");
		String lastName = authorFragment.get("lastName");
		assertThat(firstName).isEqualTo(FRANK_HERBERT.getFirstName());
		assertThat(lastName).isEqualTo(FRANK_HERBERT.getLastName());
	}

	@Test
	public void ensureGetAllWithExtractor() {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(fragment(Book::getAuthor, Author::getFirstName));
		assertThat(result).containsAllEntriesOf(
				this.books.stream().collect(
						Collectors.toMap(Book::getUuid, (bk) -> {
							Map<String, Object> fragMap = new HashMap<>();
							fragMap.put("firstName", bk.getAuthor().getFirstName());
							return new Fragment<>(fragMap);
						})));
	}

	@Test
	public void ensureGetAllWithIdAndExtractor() {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				Arrays.asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()), fragment(Book::getAuthor, Author::getFirstName));
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
	public void ensureGetAllWithFilterAndExtractor() {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				author(), fragment(Book::getAuthor, Author::getFirstName));
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
	void ensureGetAllWithFragment() {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				fragment(Book::getAuthor, Author::getFirstName, Author::getLastName));
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
	void ensureGetAllWithIdsAndFragment() {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				Arrays.asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()),
				fragment(Book::getAuthor, Author::getFirstName, Author::getLastName));
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
	void ensureGetAllWithFilterAndFragment() {
		Map<UUID, Fragment<Author>> result = this.bookRepository.getAll(
				author(), fragment(Book::getAuthor, Author::getFirstName, Author::getLastName));
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
	void ensureUpdate() {
		this.bookRepository.update(DUNE.getUuid(), Book::setPages, 700);
		assertThat(this.book.get(DUNE.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithEntityFactory() {
		this.bookRepository.update(IT.getUuid(), Book::setPages, 700, Book::new);
		assertThat(this.book.get(IT.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdAndRemoteFunction() {
		int result = this.bookRepository.update(DUNE.getUuid(), Remote.function((bk) -> {
			bk.setPages(700);
			return 700;
		}));
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(DUNE.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdRemoteFunctionAndEntityFactory() {
		int result = this.bookRepository.update(IT.getUuid(), Remote.function((bk) -> {
			bk.setPages(700);
			return 700;
		}), Book::new);
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(IT.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdAndRemoteBiFunction() {
		Remote.BiFunction<Book, Integer, Integer> function = (bk, pages) -> {
			bk.setPages(pages);
			return pages;
		};
		int result = this.bookRepository.update(DUNE.getUuid(), function, 700);
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(DUNE.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateWithIdRemoteBiFunctionAndEntityFactory() {
		Remote.BiFunction<Book, Integer, Integer> function = (bk, pages) -> {
			bk.setPages(pages);
			return pages;
		};
		int result = this.bookRepository.update(IT.getUuid(), function, 700, Book::new);
		assertThat(result).isEqualTo(700);
		assertThat(this.book.get(IT.getUuid()).getPages()).isEqualTo(700);
	}

	@Test
	void ensureUpdateAllWithFilter() {
		this.bookRepository.updateAll(author(), Book::setPages, 700);
		this.book.forEach((id, bk) -> {
			if (bk.getAuthor().equals(FRANK_HERBERT)) {
				assertThat(bk.getPages()).isEqualTo(700);
			}
		});

		assertThat(this.book.get(NAME_OF_THE_WIND.getUuid()).getPages()).isEqualTo(NAME_OF_THE_WIND.getPages());
		assertThat(this.book.get(HOBBIT.getUuid()).getPages()).isEqualTo(HOBBIT.getPages());
	}

	@Test
	void ensureUpdateAllWithFilterAndRemoteFunction() {
		Map<UUID, Integer> result = this.bookRepository.updateAll(author(), Remote.function((bk) -> {
			bk.setPages(700);
			return 700;
		}));

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
	void ensureUpdateAllWithFilterAndRemoteBiFunction() {
		Remote.BiFunction<Book, Integer, Integer> function = (bk, pages) -> {
			bk.setPages(pages);
			return pages;
		};
		Map<UUID, Integer> result = this.bookRepository.updateAll(author(), function, 700);

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
	void ensureRemoteWithReturn() {
		assertThat(this.bookRepository.delete(DUNE, true)).isEqualTo(DUNE);
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureRemoteWithNoReturn() {
		assertThat(this.bookRepository.delete(DUNE, false)).isNull();
		assertThat(this.book.containsKey(DUNE.getUuid())).isFalse();
	}

	@Test
	void ensureDeleteAllById() {
		boolean result = this.bookRepository.deleteAllById(Arrays.asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()));
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllByIdNoMatch() {
		boolean result = this.bookRepository.deleteAllById(Arrays.asList(IT.getUuid()));
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllByIdWithReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAllById(Arrays.asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()), true);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllByIdWithNoReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAllById(Arrays.asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid()), false);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithCollection() {
		boolean result = this.bookRepository.deleteAll(Arrays.asList(DUNE, DUNE_MESSIAH));
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithCollectionNoMatch() {
		boolean result = this.bookRepository.deleteAll(Collections.singletonList(IT));
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithCollectionAndReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAll(Arrays.asList(DUNE, DUNE_MESSIAH), true);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithCollectionAndNoReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAll(Collections.singletonList(IT), false);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithStream() {
		boolean result = this.bookRepository.deleteAll(Stream.of(DUNE, DUNE_MESSIAH));
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithStreamNoMatch() {
		boolean result = this.bookRepository.deleteAll(Stream.of(IT));
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithStreamAndReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAll(Stream.of(DUNE, DUNE_MESSIAH), true);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithStreamAndNoReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAll(Stream.of(DUNE, DUNE_MESSIAH), false);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithFilter() {
		boolean result = this.bookRepository.deleteAll(author());
		assertThat(result).isTrue();
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithFilterNoMatch() {
		boolean result = this.bookRepository.deleteAll(equal("author", STEPHEN_KING));
		assertThat(result).isFalse();
		assertThat(this.book.size()).isEqualTo(4);
		assertThat(this.book.values(author())).isNotEmpty();
	}

	@Test
	void ensureDeleteAllWithFilterAndReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAll(author(), true);
		Map<UUID, Book> expected = new HashMap<>();
		expected.put(DUNE.getUuid(), DUNE);
		expected.put(DUNE_MESSIAH.getUuid(), DUNE_MESSIAH);
		assertThat(result).containsAllEntriesOf(expected);
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureDeleteAllWithFilterAndNoReturn() {
		Map<UUID, Book> result = this.bookRepository.deleteAll(author(), false);
		assertThat(result).isNotNull();
		result.values().forEach((bk) -> assertThat(bk).isNull());
		assertThat(this.book.size()).isEqualTo(2);
		assertThat(this.book.values(author())).isEmpty();
	}

	@Test
	void ensureStream() {
		Set<Book> result = this.bookRepository.stream().collect(RemoteCollectors.toSet());
		assertThat(result).containsExactlyInAnyOrderElementsOf(this.book.values());
	}

	@Test
	void ensureStreamWithIds() {
		Collection<UUID> ids = Arrays.asList(DUNE.getUuid(), DUNE_MESSIAH.getUuid());
		Set<Book> result = this.bookRepository.stream(ids).collect(RemoteCollectors.toSet());
		assertThat(result).containsExactlyInAnyOrderElementsOf(this.book.values(author()));
	}

	@Test
	void ensureStreamWithFilter() {
		Set<Book> result = this.bookRepository.stream(author()).collect(RemoteCollectors.toSet());
		assertThat(result).containsExactlyInAnyOrderElementsOf(this.book.values(author()));
	}

	@Test
	void ensureCountWithFilter() {
		assertThat(this.bookRepository.count(author())).isEqualTo(2L);
	}

	@Test
	void ensureMax() {
		assertThat(this.bookRepository.max(Book::getPages)).isEqualTo(NAME_OF_THE_WIND.getPages());
	}

	@Test
	void ensureMaxWithFilter() {
		assertThat(this.bookRepository.max(author(),
				Book::getPages)).isEqualTo(DUNE.getPages());
	}

	@Test
	void ensureMaxLong() {
		assertThat(this.bookRepository.max(Book::getPagesAsLong))
				.isEqualTo(NAME_OF_THE_WIND.getPagesAsLong());
	}

	@Test
	void ensureMaxLongWithFilter() {
		assertThat(this.bookRepository.max(author(), Book::getPagesAsLong))
				.isEqualTo(DUNE.getPagesAsLong());
	}

	@Test
	void ensureMaxDouble() {
		assertThat(this.bookRepository.max(Book::getPagesAsDouble))
				.isEqualTo(NAME_OF_THE_WIND.getPagesAsDouble());
	}

	@Test
	void ensureMaxDoubleWithFilter() {
		assertThat(this.bookRepository.max(author(), Book::getPagesAsDouble))
				.isEqualTo(DUNE.getPagesAsDouble());
	}

	@Test
	void ensureMaxBigDecimal() {
		assertThat(this.bookRepository.max(Book::getPagesAsBigDecimal))
				.isEqualTo(NAME_OF_THE_WIND.getPagesAsBigDecimal());
	}

	@Test
	void ensureMaxBigDecimalWithFilter() {
		assertThat(this.bookRepository.max(author(), Book::getPagesAsBigDecimal))
				.isEqualTo(DUNE.getPagesAsBigDecimal());
	}

	@Test
	void ensureMaxWithRemoteCompareFunction() {
		assertThat(this.bookRepository.max(
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear))
				.isEqualTo(2008);
	}

	@Test
	void ensureMaxWithFilterAndRemoteCompareFunction() {
		assertThat(this.bookRepository.max(author(),
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear))
				.isEqualTo(1967);
	}

	@Test
	void ensureMaxBy() {
		assertThat(this.bookRepository.maxBy(Book::getPages)).isEqualTo(Optional.of(NAME_OF_THE_WIND));
	}

	@Test
	void ensureMaxByWithFilter() {
		assertThat(this.bookRepository.maxBy(author(), Book::getPages)).isEqualTo(Optional.of(DUNE));
	}

	@Test
	void ensureMin() {
		assertThat(this.bookRepository.min(Book::getPages)).isEqualTo(HOBBIT.getPages());
	}

	@Test
	void ensureMinWithFilter() {
		assertThat(this.bookRepository.min(author(),
				Book::getPages)).isEqualTo(DUNE_MESSIAH.getPages());
	}

	@Test
	void ensureMinLong() {
		assertThat(this.bookRepository.min(Book::getPagesAsLong))
				.isEqualTo(HOBBIT.getPagesAsLong());
	}

	@Test
	void ensureMinLongWithFilter() {
		assertThat(this.bookRepository.min(author(), Book::getPagesAsLong))
				.isEqualTo(DUNE_MESSIAH.getPagesAsLong());
	}

	@Test
	void ensureMinDouble() {
		assertThat(this.bookRepository.min(Book::getPagesAsDouble))
				.isEqualTo(HOBBIT.getPagesAsDouble());
	}

	@Test
	void ensureMinDoubleWithFilter() {
		assertThat(this.bookRepository.min(author(), Book::getPagesAsDouble))
				.isEqualTo(DUNE_MESSIAH.getPagesAsDouble());
	}

	@Test
	void ensureMinBigDecimal() {
		assertThat(this.bookRepository.min(Book::getPagesAsBigDecimal))
				.isEqualTo(HOBBIT.getPagesAsBigDecimal());
	}

	@Test
	void ensureMinBigDecimalWithFilter() {
		assertThat(this.bookRepository.min(author(), Book::getPagesAsBigDecimal))
				.isEqualTo(DUNE_MESSIAH.getPagesAsBigDecimal());
	}

	@Test
	void ensureMinWithRemoteCompareFunction() {
		assertThat(this.bookRepository.min(
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear))
				.isEqualTo(1937);
	}

	@Test
	void ensureMinWithFilterAndRemoteCompareFunction() {
		assertThat(this.bookRepository.min(author(),
				(Remote.ToComparableFunction<? super Book, Integer>) Book::getPublicationYear))
				.isEqualTo(1964);
	}

	@Test
	void ensureMinBy() {
		assertThat(this.bookRepository.minBy(Book::getPages)).isEqualTo(Optional.of(HOBBIT));
	}

	@Test
	void ensureMinByWithFilter() {
		assertThat(this.bookRepository.minBy(author(), Book::getPages)).isEqualTo(Optional.of(DUNE_MESSIAH));
	}

	@Test
	void ensureSum() {
		assertThat(this.bookRepository.sum(Book::getPages))
				.isEqualTo(books.stream().mapToLong(Book::getPages).sum());
	}

	@Test
	void ensureSumWithFilter() {
		assertThat(this.bookRepository.sum(author(),
				Book::getPages)).isEqualTo(books.stream().filter((bk) ->
					bk.getAuthor().equals(FRANK_HERBERT)).mapToInt(Book::getPages).sum());
	}

	@Test
	void ensureSumLong() {
		assertThat(this.bookRepository.sum(Book::getPagesAsLong))
				.isEqualTo(books.stream().mapToLong(Book::getPagesAsLong).sum());
	}

	@Test
	void ensureSumLongWithFilter() {
		assertThat(this.bookRepository.sum(author(), Book::getPagesAsLong))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPagesAsLong).sum());
	}

	@Test
	void ensureSumDouble() {
		assertThat(this.bookRepository.sum(Book::getPagesAsDouble))
				.isEqualTo(books.stream().mapToDouble(Book::getPagesAsDouble).sum());
	}

	@Test
	void ensureSumDoubleWithFilter() {
		assertThat(this.bookRepository.sum(author(), Book::getPagesAsDouble))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToDouble(Book::getPagesAsDouble).sum());
	}

	@Test
	void ensureSumBigDecimal() {
		assertThat(this.bookRepository.sum(Book::getPagesAsBigDecimal))
				.isEqualTo(BigDecimal.valueOf(books.stream().mapToLong(Book::getPagesAsLong).sum()));
	}

	@Test
	void ensureSumBigDecimalWithFilter() {
		assertThat(this.bookRepository.sum(author(), Book::getPagesAsBigDecimal))
				.isEqualTo(BigDecimal.valueOf(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPages).sum()));
	}

	@Test
	void ensureAverage() {
		assertThat(this.bookRepository.average(Book::getPages))
				.isEqualTo(books.stream().mapToInt(Book::getPages).average().getAsDouble());
	}

	@Test
	void ensureAverageWithFilter() {
		assertThat(this.bookRepository.average(author(),
				Book::getPages)).isEqualTo(books.stream().filter((bk) ->
				bk.getAuthor().equals(FRANK_HERBERT)).mapToInt(Book::getPages).average().getAsDouble());
	}

	@Test
	void ensureAverageLong() {
		assertThat(this.bookRepository.average(Book::getPagesAsLong))
				.isEqualTo(books.stream().mapToLong(Book::getPagesAsLong).average().getAsDouble());
	}

	@Test
	void ensureAverageLongWithFilter() {
		assertThat(this.bookRepository.average(author(), Book::getPagesAsLong))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPagesAsLong).average().getAsDouble());
	}

	@Test
	void ensureAverageDouble() {
		assertThat(this.bookRepository.average(Book::getPagesAsDouble))
				.isEqualTo(books.stream().mapToDouble(Book::getPagesAsDouble).average().getAsDouble());
	}

	@Test
	void ensureAverageDoubleWithFilter() {
		assertThat(this.bookRepository.average(author(), Book::getPagesAsDouble))
				.isEqualTo(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToDouble(Book::getPagesAsDouble).average().getAsDouble());
	}

	@Test
	void ensureAverageBigDecimal() {
		assertThat(this.bookRepository.average(Book::getPagesAsBigDecimal))
				.isCloseTo(BigDecimal.valueOf(books.stream().mapToLong(Book::getPagesAsLong).average().getAsDouble()), Percentage.withPercentage(.1));
	}

	@Test
	void ensureAverageBigDecimalWithFilter() {
		assertThat(this.bookRepository.average(author(), Book::getPagesAsBigDecimal))
				.isCloseTo(BigDecimal.valueOf(books.stream().filter((bk) ->
						bk.getAuthor().equals(FRANK_HERBERT)).mapToLong(Book::getPages).average().getAsDouble()), Percentage.withPercentage(.1));
	}

	@Test
	void ensureDistinct() {
		List authors = Arrays.asList(FRANK_HERBERT, JOHN_TOLKIEN, PATRICK_ROTHFUSS);
		assertThat(this.bookRepository.distinct(extract("author"))).containsExactlyInAnyOrderElementsOf(authors);
	}

	@Test
	void ensureDistinctFilter() {
		List authors = Arrays.asList(FRANK_HERBERT, JOHN_TOLKIEN, PATRICK_ROTHFUSS);
		assertThat(this.bookRepository.distinct(Filters.greater(Book::getPages, 100), extract("author")))
				.containsExactlyInAnyOrderElementsOf(authors);
	}

	@Test
	void ensureGroupBy() {
		Map<Author, Set<Book>> result = this.bookRepository.groupBy(extract("author"));
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, toSet())));
	}

	@Test
	void ensureGroupByWithSort() {
		Map<Author, SortedSet<Book>> result = this.bookRepository.groupBy(extract("author"),
				Remote.comparator((Remote.Comparator<Book>) (o1, o2) -> o1.getTitle().compareTo(o2.getTitle())));
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, toCollection(() -> (SortedSet<Book>) new TreeSet<>(Comparator.comparing(Book::getTitle))))));
		assertThat(result.get(FRANK_HERBERT)).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureGroupByWithFilter() {
		Map<Author, Set<Book>> result = this.bookRepository.groupBy(Filters.greater(extract("pages"), 500), extract("author"));
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getPages() > 500)
				.collect(Collectors.groupingBy(Book::getAuthor, toSet())));
	}

	@Test
	void ensureGroupByWithFilterAndSort() {
		Map<Author, SortedSet<Book>> result = this.bookRepository.groupBy(Filters.greater(extract("pages"), 500), extract("author"),
				Remote.comparator((Remote.Comparator<Book>) (o1, o2) -> o1.getTitle().compareTo(o2.getTitle())));
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getPages() > 500)
				.collect(Collectors.groupingBy(Book::getAuthor, toCollection(() -> (SortedSet<Book>) new TreeSet<>(Comparator.comparing(Book::getTitle))))));
		assertThat(result.get(FRANK_HERBERT)).containsExactly(DUNE);
	}

	@Test
	void ensureGroupByWithRemoteCollector() {
		Map<Author, Integer> result = this.bookRepository.groupBy(extract("author"), RemoteCollectors.summingInt(Book::getPages));
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureGroupByWithFilterAndRemoteCollector() {
		Map<Author, Integer> result = this.bookRepository.groupBy(author(), extract("author"), RemoteCollectors.summingInt(Book::getPages));
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getAuthor().equals(FRANK_HERBERT))
				.collect(Collectors.groupingBy(Book::getAuthor,  Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureGroupByWithRemoteCollectorAndMapFactory() {
		Map<Author, Integer> result = this.bookRepository.groupBy(extract("author"), Remote.supplier(TreeMap::new), RemoteCollectors.summingInt(Book::getPages));
		assertThat(result).isInstanceOf(TreeMap.class);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureGroupByWithFilterRemoteCollectorAndMapFactory() {
		Map<Author, Integer> result = this.bookRepository.groupBy(Filters.greater(extract("pages"), 500),
				extract("author"), Remote.supplier(TreeMap::new),
				RemoteCollectors.summingInt(Book::getPages));
		assertThat(result).isInstanceOf(TreeMap.class);
		assertThat(result).containsAllEntriesOf(this.books.stream()
				.filter((bk) -> bk.getPages() > 500)
				.collect(Collectors.groupingBy(Book::getAuthor, Collectors.summingInt(Book::getPages))));
	}

	@Test
	void ensureTop() {
		List<Integer> topPages = this.bookRepository.top(Book::getPages, 3);
		assertThat(topPages).containsExactly(742, 677, 468);
	}

	@Test
	void ensureTopWithFilter() {
		List<Integer> topPages = this.bookRepository.top(author(), Book::getPages, 3);
		assertThat(topPages).containsExactly(677, 468);
	}

	@Test
	void ensureTopWithRemoteComparator() {
		List<Integer> topPages = this.bookRepository.top(Book::getPages,
				Remote.Comparator.reverseOrder(),
				3);
		assertThat(topPages).containsExactly(355, 468, 677);
	}

	@Test
	void ensureTopWithFilterAndRemoteComparator() {
		List<Integer> topPages = this.bookRepository.top(author(), Book::getPages,
				Remote.Comparator.reverseOrder(),
				3);
		assertThat(topPages).containsExactly(468, 677);
	}

	@Test
	void ensureTopBy() {
		List<Book> topPages = this.bookRepository.topBy(Book::getPages, 3);
		assertThat(topPages).containsExactly(NAME_OF_THE_WIND, DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureTopByFilter() {
		List<Book> topPages = this.bookRepository.topBy(author(), Book::getPages, 3);
		assertThat(topPages).containsExactly(DUNE, DUNE_MESSIAH);
	}

	@Test
	void ensureTopByWithRemoteComparator() {
		List<Book> topPages = this.bookRepository.topBy(Remote.Comparator.comparing(Book::getTitle), 3);
		assertThat(topPages).containsExactly(NAME_OF_THE_WIND, HOBBIT, DUNE_MESSIAH);
	}

	@Test
	void ensureTopByWithFilterAndRemoteComparator() {
		List<Book> topPages = this.bookRepository.topBy(Filters.greater(Book::getPages, 500), Remote.Comparator.comparing(Book::getTitle), 3);
		assertThat(topPages).containsExactly(NAME_OF_THE_WIND, DUNE);
	}

	// ----- helper methods -------------------------------------------------

	Filter<Author> author() {
		return equal("author", AbstractDataTest.FRANK_HERBERT);
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceRepositories("com.oracle.coherence.spring.data.model.repositories")
	public static class Config {
	}
}
