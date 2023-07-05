/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.repository;

import java.util.Collection;
import java.util.List;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.data.AbstractDataTests;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.BookProjection;
import com.oracle.coherence.spring.data.model.NestedBookProjection;
import com.oracle.coherence.spring.data.model.NestedOpenBookProjection;
import com.oracle.coherence.spring.data.model.OpenBookProjection;
import com.oracle.coherence.spring.data.model.PublicationYearClassProjection;
import com.oracle.coherence.spring.data.model.repositories.BookProjectionRepository;
import com.oracle.coherence.spring.data.query.QueryFinderTests;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the various Projections, interface-based and class-based projections (DTO).
 * @author Vaso Putica
 * @author Gunnar Hillert
 */
@SpringJUnitConfig(QueryFinderTests.Config.class)
@DirtiesContext
public class ProjectionTests extends AbstractDataTests {
	@Inject
	protected BookProjectionRepository bookRepository;

	@Test
	public void ensureInterfaceProjection() {
		List<BookProjection> books = this.bookRepository.findByPages(468);
		assertThat(books.size()).isEqualTo(1);
		BookProjection book = books.get(0);
		assertThat(BookProjection.class.isAssignableFrom(book.getClass())).isTrue();
		assertThat(book.getTitle()).isEqualTo("Dune Messiah");
		assertThat(book.getTitleSequence()).isEqualTo("Dune Messiah");
		assertThat(book.getPages()).isEqualTo(468);
		BookProjection.AuthorSummary author = book.getAuthor();
		assertThat(author).isNotNull();
		assertThat(author.getFirstName()).isEqualTo("Frank");
		BookProjection.AddressProjection address = author.getAddress();
		assertThat(address).isNotNull();
		assertThat(address.getStreet()).isEqualTo("Shirley Road");
		assertThat(address.getNumber()).isInstanceOf(String.class);
	}

	@Test
	public void ensureNestedInterfaceProjection() {
		List<NestedBookProjection> books = this.bookRepository.findByTitleContains("Hobbit");
		assertThat(books.size()).isEqualTo(1);
		NestedBookProjection book = books.get(0);
		assertThat(book).isInstanceOf(NestedBookProjection.class);
		assertThat(book.getTitle()).isEqualTo("The Hobbit");
		NestedBookProjection.AuthorSummary author = book.getAuthor();
		assertThat(author.getFirstName()).isEqualTo("John");
		assertThat(author.getUpperFirstName()).isEqualTo("JOHN");
		NestedBookProjection.AuthorSummary.AddressProjection address = author.getAddress();
		assertThat(address.getStreet()).isEqualTo("Johnson Ave");
	}

	@Test
	public void ensureNestedOpenInterfaceProjection() {
		List<NestedOpenBookProjection> books = this.bookRepository.findByTitleEndingWith("Wind");
		assertThat(books.size()).isEqualTo(1);
		NestedOpenBookProjection book = books.get(0);
		assertThat(book).isInstanceOf(NestedOpenBookProjection.class);
		assertThat(book.getTitle()).isEqualTo("The Name of the Wind");
		NestedOpenBookProjection.AuthorSummary author = book.getAuthor();
		assertThat(author.getFirstName()).isEqualTo("Patrick");
		assertThat(author.getFullName()).hasValue("Patrick Rothfuss");
		NestedOpenBookProjection.AuthorSummary.AddressProjection address = author.getAddress();
		assertThat(address.getStreet()).isEqualTo("Strawberry Rd");
	}

	@Test
	public void ensureOpenRootInterfaceProjection() {
		List<OpenBookProjection> books = this.bookRepository.findByTitle("Dune");
		assertThat(books.size()).isEqualTo(1);
		OpenBookProjection book = books.get(0);
		assertThat(book).isInstanceOf(OpenBookProjection.class);
		assertThat(book.getBasics()).hasValue("Author: Frank, Title: Dune, Pages: 677");
		assertThat(book.getTitle()).isEqualTo("Dune");
		OpenBookProjection.AuthorSummary author = book.getAuthor();
		assertThat(author.getFullName()).hasValue("Frank Herbert");
		OpenBookProjection.AuthorSummary.AddressProjection address = author.getAddress();
		assertThat(address.getStreet()).isEqualTo("Shirley Road");
	}

	@Test
	public void ensureClassBasedProjection() {
		List<PublicationYearClassProjection> books = this.bookRepository.findByPublished(IT.getPublished());
		assertThat(books.size()).isEqualTo(1);
		PublicationYearClassProjection book = books.get(0);
		assertThat(book.getPublished()).isEqualTo(IT.getPublished());
		assertThat(book.getPublicationYear()).isEqualTo(1967);
	}

	@Test
	public void ensureDynamicProjection() {
		Collection<Book> books = this.bookRepository.findByTitle("Dune", Book.class);
		assertThat(books.size()).isEqualTo(1);
		Book book = books.iterator().next();
		assertThat(book).isInstanceOf(Book.class);
		assertThat(book.getTitle()).isEqualTo("Dune");
		assertThat(book.getAuthor().getFirstName()).isEqualTo("Frank");

		Collection<BookProjection> bookProjections = this.bookRepository.findByTitle("Dune", BookProjection.class);
		assertThat(bookProjections.size()).isEqualTo(1);
		BookProjection bookProjection = bookProjections.iterator().next();
		assertThat(bookProjection).isInstanceOf(BookProjection.class);
		assertThat(bookProjection.getTitle()).isEqualTo("Dune");
		BookProjection.AuthorSummary author = bookProjection.getAuthor();
		assertThat(author).isNotNull();
		assertThat(author.getFirstName()).isEqualTo("Frank");
		BookProjection.AddressProjection address = author.getAddress();
		assertThat(address).isNotNull();
		assertThat(address.getStreet()).isEqualTo("Shirley Road");
		assertThat(address.getNumber()).isInstanceOf(String.class);

		Collection<OpenBookProjection> openBookProjections = this.bookRepository.findByTitle("Dune", OpenBookProjection.class);
		assertThat(openBookProjections.size()).isEqualTo(1);
		OpenBookProjection openBookProjection = openBookProjections.iterator().next();
		assertThat(openBookProjection).isInstanceOf(OpenBookProjection.class);
		assertThat(openBookProjection.getBasics()).hasValue("Author: Frank, Title: Dune, Pages: 677");
		assertThat(openBookProjection.getTitle()).isEqualTo("Dune");
		OpenBookProjection.AuthorSummary openBookProjectionAuthor = openBookProjection.getAuthor();
		assertThat(openBookProjectionAuthor).isInstanceOf(OpenBookProjection.AuthorSummary.class);
		assertThat(openBookProjectionAuthor.getFullName()).hasValue("Frank Herbert");
		OpenBookProjection.AuthorSummary.AddressProjection openBookProjectionAuthorAddress = openBookProjectionAuthor.getAddress();
		assertThat(openBookProjectionAuthorAddress.getStreet()).isEqualTo("Shirley Road");

		Collection<NestedBookProjection> nestedBookProjections = this.bookRepository.findByTitle("The Name of the Wind", NestedBookProjection.class);
		assertThat(nestedBookProjections.size()).isEqualTo(1);
		NestedBookProjection nestedBookProjection = nestedBookProjections.iterator().next();
		assertThat(nestedBookProjection).isInstanceOf(NestedBookProjection.class);
		assertThat(nestedBookProjection.getTitle()).isEqualTo("The Name of the Wind");
		NestedBookProjection.AuthorSummary nestedBookProjectionAuthor = nestedBookProjection.getAuthor();
		assertThat(nestedBookProjectionAuthor.getFirstName()).isEqualTo("Patrick");
		NestedBookProjection.AuthorSummary.AddressProjection nestedBookProjectionAuthorAddress = nestedBookProjectionAuthor.getAddress();
		assertThat(nestedBookProjectionAuthorAddress.getStreet()).isEqualTo("Strawberry Rd");

		Collection<NestedOpenBookProjection> nestedOpenBookProjections = this.bookRepository.findByTitle("Dune Messiah", NestedOpenBookProjection.class);
		assertThat(nestedOpenBookProjections.size()).isEqualTo(1);
		NestedOpenBookProjection nestedOpenBook = nestedOpenBookProjections.iterator().next();
		assertThat(nestedOpenBook).isInstanceOf(NestedOpenBookProjection.class);
		assertThat(nestedOpenBook.getTitle()).isEqualTo("Dune Messiah");
		NestedOpenBookProjection.AuthorSummary nestedOpenBookAuthor = nestedOpenBook.getAuthor();
		assertThat(nestedOpenBookAuthor.getFirstName()).isEqualTo("Frank");
		assertThat(nestedOpenBookAuthor.getFullName()).hasValue("Frank Herbert");
		NestedOpenBookProjection.AuthorSummary.AddressProjection nestedOpenBookAuthorAddress = nestedOpenBookAuthor.getAddress();
		assertThat(nestedOpenBookAuthorAddress.getStreet()).isEqualTo("Shirley Road");
	}

	@Test
	void ensureFindAllOrdered() {
		List<BookProjection> books = this.bookRepository.findBy(Sort.by(Sort.Direction.DESC, "pages"));
		assertThat(books.size()).isEqualTo(4);
		assertThat(books.get(0)).isInstanceOf(BookProjection.class);
		assertThat(books.get(0).getTitle()).isEqualTo(AbstractDataTests.NAME_OF_THE_WIND.getTitle());
		assertThat(books.get(1).getTitle()).isEqualTo(AbstractDataTests.DUNE.getTitle());
		assertThat(books.get(2).getTitle()).isEqualTo(AbstractDataTests.DUNE_MESSIAH.getTitle());
		assertThat(books.get(3).getTitle()).isEqualTo(AbstractDataTests.HOBBIT.getTitle());

		books = this.bookRepository.findByOrderByPagesAsc();
		assertThat(books.size()).isEqualTo(4);
		assertThat(books.get(0)).isInstanceOf(BookProjection.class);
		assertThat(books.get(0).getTitle()).isEqualTo(AbstractDataTests.HOBBIT.getTitle());
		assertThat(books.get(1).getTitle()).isEqualTo(AbstractDataTests.DUNE_MESSIAH.getTitle());
		assertThat(books.get(2).getTitle()).isEqualTo(AbstractDataTests.DUNE.getTitle());
		assertThat(books.get(3).getTitle()).isEqualTo(AbstractDataTests.NAME_OF_THE_WIND.getTitle());
	}

	@Test
	void ensureOrderedAscQuery() {
		List<BookProjection> books = this.bookRepository.findByAuthorOrderByPagesAsc(FRANK_HERBERT);
		assertThat(books.size()).isEqualTo(2);
		assertThat(books.get(0)).isInstanceOf(BookProjection.class);
		assertThat(books.get(0).getTitle()).isEqualTo(AbstractDataTests.DUNE_MESSIAH.getTitle());
		assertThat(books.get(1).getTitle()).isEqualTo(AbstractDataTests.DUNE.getTitle());

		List<NestedBookProjection> nestedBooks = this.bookRepository.findByAuthor(FRANK_HERBERT, Sort.by(Sort.Direction.ASC, "pages"));
		assertThat(nestedBooks.size()).isEqualTo(2);
		assertThat(nestedBooks.get(0).getTitle()).isEqualTo(AbstractDataTests.DUNE_MESSIAH.getTitle());
		assertThat(nestedBooks.get(1).getTitle()).isEqualTo(AbstractDataTests.DUNE.getTitle());
	}

	@Test
	void ensureOrderedDescQuery() {
		List<NestedBookProjection> books = this.bookRepository.findByAuthorOrderByPagesDesc(FRANK_HERBERT);
		assertThat(books.size()).isEqualTo(2);
		assertThat(books.get(0)).isInstanceOf(NestedBookProjection.class);
		assertThat(books.get(0).getTitle()).isEqualTo(AbstractDataTests.DUNE.getTitle());
		assertThat(books.get(1).getTitle()).isEqualTo(AbstractDataTests.DUNE_MESSIAH.getTitle());

		books = this.bookRepository.findByAuthor(FRANK_HERBERT, Sort.by(Sort.Direction.DESC, "pages"));
		assertThat(books.size()).isEqualTo(2);
		assertThat(books.get(0)).isInstanceOf(NestedBookProjection.class);
		assertThat(books.get(0).getTitle()).isEqualTo(AbstractDataTests.DUNE.getTitle());
		assertThat(books.get(1).getTitle()).isEqualTo(AbstractDataTests.DUNE_MESSIAH.getTitle());
	}

	@Test
	void ensureStartingWithOrder() {
		List<OpenBookProjection> books = this.bookRepository.findByTitleStartingWithOrderByPagesDesc("The");
		assertThat(books.size()).isEqualTo(2);
		assertThat(books.get(0)).isInstanceOf(OpenBookProjection.class);
		assertThat(books.get(0).getAuthor().getFullName()).hasValue("Patrick Rothfuss");
		assertThat(books.get(1).getAuthor().getFullName()).hasValue("John Tolkien");
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceRepositories("com.oracle.coherence.spring.data.model.repositories")
	public static class Config {
	}
}
