package com.oracle.coherence.spring.data.repository;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.oracle.coherence.spring.configuration.annotation.EnableCoherence;
import com.oracle.coherence.spring.data.AbstractDataTest;
import com.oracle.coherence.spring.data.config.EnableCoherenceRepositories;
import com.oracle.coherence.spring.data.model.Book;
import com.oracle.coherence.spring.data.model.BookProjection;
import com.oracle.coherence.spring.data.model.CalendarProjection;
import com.oracle.coherence.spring.data.model.NestedBookProjection;
import com.oracle.coherence.spring.data.model.NestedOpenBookProjection;
import com.oracle.coherence.spring.data.model.OpenBookProjection;
import com.oracle.coherence.spring.data.model.repositories.BookProjectionRepository;
import com.oracle.coherence.spring.data.query.QueryFinderTests;
import org.junit.jupiter.api.Test;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(QueryFinderTests.Config.class)
@DirtiesContext
public class ProjectionTest extends AbstractDataTest {
	@Inject
	protected BookProjectionRepository bookRepository;

	@Test
	public void ensureInterfaceProjection() {
		List<BookProjection> books = this.bookRepository.findByPages(468);
		assertThat(books.size()).isEqualTo(1);
		BookProjection book = books.get(0);
		assertThat(book.getTitle()).isEqualTo("Dune Messiah");
		assertThat(book.getTitleSequence()).isEqualTo("Dune Messiah");
		assertThat(book.getPages()).isEqualTo(468);
		BookProjection.AuthorSummary author = book.getAuthor();
		assertThat(author).isNotNull();
		assertThat(author.getFirstName()).isEqualTo("Frank");
		// fails as Author::getLowerFirstName returns CharSequence while
		// BookProjection.AuthorSummary::getLowerFirstName returns Serializable
		// it isn't assignable and Serializable interface has no methods
		// so fragment can't be created
//        assertThat(author.getLowerFirstName()).isEqualTo("frank");
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
		assertThat(book.getBasics()).hasValue("Author: Frank, Title: Dune, Pages: 677");
		assertThat(book.getTitle()).isEqualTo("Dune");
		OpenBookProjection.AuthorSummary author = book.getAuthor();
		assertThat(author.getFullName()).hasValue("Frank Herbert");
		OpenBookProjection.AuthorSummary.AddressProjection address = author.getAddress();
		assertThat(address.getStreet()).isEqualTo("Shirley Road");
	}

	@Test
	public void ensureClassBasedProjection() {
		List<CalendarProjection> books = this.bookRepository.findByPublished(IT.getPublished());
		assertThat(books.size()).isEqualTo(1);
		CalendarProjection book = books.get(0);
		assertThat(book.getPublished()).isEqualByComparingTo(IT.getPublished());
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
		assertThat(openBookProjection.getBasics()).hasValue("Author: Frank, Title: Dune, Pages: 677");
		assertThat(openBookProjection.getTitle()).isEqualTo("Dune");
		OpenBookProjection.AuthorSummary openBookProjectionAuthor = openBookProjection.getAuthor();
		assertThat(openBookProjectionAuthor.getFullName()).hasValue("Frank Herbert");
		OpenBookProjection.AuthorSummary.AddressProjection openBookProjectionAuthorAddress = openBookProjectionAuthor.getAddress();
		assertThat(openBookProjectionAuthorAddress.getStreet()).isEqualTo("Shirley Road");

		Collection<NestedBookProjection> nestedBookProjections = this.bookRepository.findByTitle("The Name of the Wind", NestedBookProjection.class);
		assertThat(nestedBookProjections.size()).isEqualTo(1);
		NestedBookProjection nestedBookProjection = nestedBookProjections.iterator().next();
		assertThat(nestedBookProjection.getTitle()).isEqualTo("The Name of the Wind");
		NestedBookProjection.AuthorSummary nestedBookProjectionAuthor = nestedBookProjection.getAuthor();
		assertThat(nestedBookProjectionAuthor.getFirstName()).isEqualTo("Patrick");
		NestedBookProjection.AuthorSummary.AddressProjection nestedBookProjectionAuthorAddress = nestedBookProjectionAuthor.getAddress();
		assertThat(nestedBookProjectionAuthorAddress.getStreet()).isEqualTo("Strawberry Rd");

		Collection<NestedOpenBookProjection> nestedOpenBookProjections = this.bookRepository.findByTitle("Dune Messiah", NestedOpenBookProjection.class);
		assertThat(nestedOpenBookProjections.size()).isEqualTo(1);
		NestedOpenBookProjection nestedOpenBook = nestedOpenBookProjections.iterator().next();
		assertThat(nestedOpenBook.getTitle()).isEqualTo("Dune Messiah");
		NestedOpenBookProjection.AuthorSummary nestedOpenBookAuthor = nestedOpenBook.getAuthor();
		assertThat(nestedOpenBookAuthor.getFirstName()).isEqualTo("Frank");
		assertThat(nestedOpenBookAuthor.getFullName()).hasValue("Frank Herbert");
		NestedOpenBookProjection.AuthorSummary.AddressProjection nestedOpenBookAuthorAddress = nestedOpenBookAuthor.getAddress();
		assertThat(nestedOpenBookAuthorAddress.getStreet()).isEqualTo("Shirley Road");
	}

	@Configuration
	@EnableCoherence
	@EnableCoherenceRepositories("com.oracle.coherence.spring.data.model.repositories")
	public static class Config {
	}
}
