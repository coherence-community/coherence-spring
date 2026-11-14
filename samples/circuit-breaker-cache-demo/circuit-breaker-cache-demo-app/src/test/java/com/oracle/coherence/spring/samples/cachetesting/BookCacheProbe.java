/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.cachetesting;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.oracle.coherence.spring.samples.circuitbreaker.CircuitBreakerApplication;
import com.oracle.coherence.spring.samples.circuitbreaker.dao.BookRepository;
import com.oracle.coherence.spring.samples.circuitbreaker.model.Book;
import com.tangosol.io.DefaultSerializer;
import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ExternalizableHelper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerEvent;

import org.springframework.boot.SpringApplication;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fresh-JVM probe; only this test package is placed on the application classpath.
 * No test security metadata is installed. The negative JVM omits the production policy.
 */
public final class BookCacheProbe {

	private BookCacheProbe() {
	}

	public static void main(String[] args) throws Exception {
		boolean permitted = Boolean.parseBoolean(args[0]);
		boolean enforced = policyBoundary(permitted);
		SpringApplication application = new SpringApplication(CircuitBreakerApplication.class);
		try (var context = application.run("--server.port=0", "--coherence.sessions.server[0].config=" + args[1])) {
			CacheManager manager = context.getBean(CacheManager.class);
			Cache books = manager.getCache("books");
			Cache allBooks = manager.getCache("allBooks");
			assertThat(books).isNotNull();
			assertThat(allBooks).isNotNull();
			NamedCache<?, ?> nativeCache = (NamedCache<?, ?>) books.getNativeCache();
			assertThat(nativeCache.getCacheService().getInfo().getServiceName()).contains("ExtendTcpCacheService");
			assertThat(nativeCache.getCacheService().getSerializer()).isInstanceOf(DefaultSerializer.class);
			Book original = context.getBean(BookRepository.class).findById(1L).orElseThrow();
			assertThat(original.getId()).isEqualTo(1L);
			assertThat(original.getTitle()).isEqualTo("Title");
			assertThat(original.getAuthor()).isEqualTo("author");
			assertThat(original.getPages()).isEqualTo(122);
			assertThat(original.getPublished()).isNotNull();
			boolean rejected = enforced && !permitted;
			if (rejected) {
				assertThatThrownBy(() -> books.get(1L)).hasRootCauseInstanceOf(InvalidClassException.class);
			}
			else {
				assertBook((Book) books.get(1L).get(), original);
			}
			CircuitBreaker breaker = context.getBean(CircuitBreakerRegistry.class).circuitBreaker("coherence");
			List<CircuitBreakerEvent.Type> events = new ArrayList<>();
			breaker.getEventPublisher().onEvent((event) -> events.add(event.getEventType()));
			JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
			String base = "http://127.0.0.1:" + context.getEnvironment().getProperty("local.server.port");
			assertTitle(base + "/api/books/1", "Title");
			assertThat(jdbc.update("update BOOKS set TITLE=? where ID=1", "DATABASE_ONLY_1")).isEqualTo(1);
			assertTitle(base + "/api/books/1", rejected ? "DATABASE_ONLY_1" : "Title");
			assertTitle(base + "/api/books", "DATABASE_ONLY_1");
			assertThat(jdbc.update("update BOOKS set TITLE=? where ID=1", "DATABASE_ONLY_2")).isEqualTo(1);
			assertTitle(base + "/api/books", rejected ? "DATABASE_ONLY_2" : "DATABASE_ONLY_1");
			if (rejected) {
				assertThatThrownBy(() -> allBooks.get(SimpleKey.EMPTY)).hasRootCauseInstanceOf(InvalidClassException.class);
				assertThat(events).containsExactly(CircuitBreakerEvent.Type.ERROR, CircuitBreakerEvent.Type.ERROR,
						CircuitBreakerEvent.Type.SUCCESS, CircuitBreakerEvent.Type.ERROR);
			}
			else {
				Object value = allBooks.get(SimpleKey.EMPTY).get();
				assertThat(value).isExactlyInstanceOf(ArrayList.class);
				List<?> restored = (List<?>) value;
				assertThat(restored).hasSize(2);
				original.setTitle("DATABASE_ONLY_1");
				assertBook((Book) restored.get(0), original);
				Book second = context.getBean(BookRepository.class).findById(2L).orElseThrow();
				assertBook((Book) restored.get(1), second);
				assertThat(events).containsExactly(CircuitBreakerEvent.Type.SUCCESS, CircuitBreakerEvent.Type.SUCCESS,
						CircuitBreakerEvent.Type.SUCCESS, CircuitBreakerEvent.Type.SUCCESS);
			}
			assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
			System.out.println("BOOK_CACHE_PROBE_PASS permitted=" + permitted + " enforced=" + enforced
					+ " events=" + events + " java=" + System.getProperty("java.runtime.version")
					+ " mode=" + System.getProperty("coherence.security.mode"));
		}
		finally {
			Coherence.closeAll();
		}
	}

	private static void assertTitle(String url, String title) throws Exception {
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		var response = client.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10)).build(),
				HttpResponse.BodyHandlers.ofString());
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).contains("\"title\":\"" + title + "\"");
	}

	private static void assertBook(Book actual, Book expected) {
		assertThat(actual).isExactlyInstanceOf(Book.class);
		assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
	}

	private static boolean policyBoundary(boolean permitted) throws Exception {
		try {
			Class<?> config = Class.forName("com.tangosol.internal.util.security.SecurityConfig");
			Object policy = config.getMethod("current").invoke(null);
			assertThat(config.getMethod("contains", String.class).invoke(policy, Book.class.getName())).isEqualTo(permitted);
			assertThat(config.getMethod("isExecutable", String.class).invoke(policy, Book.class.getName())).isEqualTo(false);
			if (permitted) {
				assertThat(config.getMethod("source", String.class).invoke(policy, Book.class.getName())).isEqualTo("manual");
			}
			Class<?> executable = Class.forName("com.tangosol.util.RemoteExecutablePolicy");
			assertThat(executable.getMethod("isExecutable", Class.class)
					.invoke(executable.getMethod("current").invoke(null), Book.class)).isEqualTo(false);
			Class<?> allowlist = Class.forName("com.tangosol.io.internal.SerializationAllowlist");
			assertThat(allowlist.getMethod("isAllowlisted", Class.class).invoke(null, Book.class)).isEqualTo(permitted);
			assertThat(allowlist.getMethod("isAllowlisted", Class.class).invoke(null, UnlistedPayload.class)).isEqualTo(false);
			boolean enforced = (boolean) Class.forName("com.tangosol.util.CoherenceMode")
					.getMethod("isSecurityHardeningEnabled").invoke(null);
			assertThat(System.getProperty("coherence.security.mode")).isIn(null, "hardened");
			assertThat(enforced).isTrue();
			DefaultSerializer serializer = new DefaultSerializer();
			var binary = ExternalizableHelper.toBinary(new UnlistedPayload(), serializer);
			assertThatThrownBy(() -> ExternalizableHelper.fromBinary(binary, serializer))
					.hasRootCauseInstanceOf(InvalidClassException.class);
			assertThat(UnlistedPayload.readCount).isZero();
			return enforced;
		}
		catch (ClassNotFoundException ex) {
			assertThat(ex.getMessage()).isEqualTo("com.tangosol.internal.util.security.SecurityConfig");
			return false;
		}
	}

	private static final class UnlistedPayload implements Serializable {

		private static final long serialVersionUID = 1L;

		private static int readCount;

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			readCount++;
		}
	}
}
