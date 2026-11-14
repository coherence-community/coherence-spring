/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.cacheprobe;

import java.io.InvalidClassException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.oracle.coherence.spring.demo.CoherenceApp;
import com.oracle.coherence.spring.demo.dao.EventRepository;
import com.oracle.coherence.spring.demo.model.Event;
import com.oracle.coherence.spring.demo.model.Person;
import com.oracle.coherence.spring.demo.service.EventService;
import com.oracle.coherence.spring.demo.service.PersonService;
import com.tangosol.io.DefaultSerializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Coherence;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.NamedCache;
import com.tangosol.util.ExternalizableHelper;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Pageable;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** CachePut's actual ORM-managed Event graph, received over a separate member boundary. */
public final class CloudConfigCacheProbe {

	private CloudConfigCacheProbe() {
	}

	public static void main(String[] args) throws Exception {
		if (args[0].equals("storage")) {
			if (CacheRuntime.observe()) {
				policy(args[1]);
			}
			Coherence.main(new String[0]);
			return;
		}
		try (var context = SpringApplication.run(CoherenceApp.class, Arrays.copyOfRange(args, 2, args.length))) {
			boolean hardened = CacheRuntime.observe();
			if (hardened) {
				policy(args[1]);
			}
			assertThat(context.getEnvironment().getProperty("cache.it.marker")).isEqualTo("isolated-config-server");
			assertThat(CacheFactory.getCluster().getLocalMember().getMemberName()).isEqualTo("CloudConfigCacheIT");
			var factory = context.getBean(EntityManagerFactory.class).unwrap(SessionFactoryImplementor.class);
			assertThat(factory.getSessionFactoryOptions().isSecondLevelCacheEnabled()).isFalse();
			assertThat(factory.getSessionFactoryOptions().isQueryCacheEnabled()).isFalse();
			System.out.println("CLOUD_ORM " + org.hibernate.Version.getVersionString() + " regionFactory=" + factory.getCache().getRegionFactory());
			var jdbc = context.getBean(JdbcTemplate.class);
			var people = context.getBean(PersonService.class);
			long personId = people.createAndStorePerson("Ada", "Lovelace", 36);
			var firstPerson = people.listPeople(Pageable.unpaged()).getContent().get(0);
			assertThat(jdbc.update("update PEOPLE set FIRSTNAME='database person' where ID=?", personId)).isEqualTo(1);
			factory.getStatistics().clear();
			var secondPerson = people.listPeople(Pageable.unpaged()).getContent().get(0);
			assertThat(secondPerson).isNotSameAs(firstPerson);
			assertThat(secondPerson.getFirstname()).isEqualTo("database person");
			assertThat(factory.getStatistics().getPrepareStatementCount()).isPositive();
			var service = context.getBean(EventService.class);
			Event saved = service.createAndStoreEvent("cached title", new Date(1000));
			assertThat(saved.getParticipants()).isEmpty();
			CacheRuntime.descriptors(saved);
			var cache = context.getBean(CacheManager.class).getCache("events");
			assertThat(cache).isNotNull();
			NamedCache<?, ?> nativeCache = (NamedCache<?, ?>) cache.getNativeCache();
			assertThat(nativeCache.getCacheService().getInfo().getServiceName()).isEqualTo("PartitionedCache");
			assertThat(((DistributedCacheService) nativeCache.getCacheService()).isLocalStorageEnabled()).isFalse();
			assertThat(nativeCache.getCacheService().getSerializer()).isInstanceOf(DefaultSerializer.class);
			assertThat(jdbc.update("update EVENTS set TITLE='database title' where ID=?", saved.getId())).isEqualTo(1);
			factory.getStatistics().clear();
			if (hardened && !args[1].equals("production") && !args[1].equals("Timestamp")) {
				assertThatThrownBy(() -> cache.get(saved.getId())).hasRootCauseInstanceOf(InvalidClassException.class);
				System.out.println("CLOUD_PERMISSION_REJECTED " + args[1]);
				// Eviction returns the denied value; clear has no payload response.
				nativeCache.clear();
			}
			else {
				Event restored = (Event) cache.get(saved.getId()).get();
				assertThat(restored).isNotSameAs(saved);
				assertThat(restored.getTitle()).isEqualTo("cached title");
				assertThat(restored.getId()).isEqualTo(saved.getId());
				assertThat(restored.getDate()).isEqualTo(saved.getDate());
				assertThat(restored.getParticipants()).isEmpty();
				assertThat(factory.getStatistics().getPrepareStatementCount()).isZero();
				System.out.println("CLOUD_EVENT_HIT SQL=0 differentIdentity=true title=" + restored.getTitle());
				assertThat(AopUtils.isAopProxy(service)).isTrue();
				factory.getStatistics().clear();
				Event serviceHit = service.getEvent(saved.getId());
				assertThat(serviceHit).isNotSameAs(saved);
				assertThat(serviceHit.getTitle()).isEqualTo("cached title");
				assertThat(factory.getStatistics().getPrepareStatementCount()).isZero();
				System.out.println("CLOUD_SERVICE_HIT SQL=0 title=" + serviceHit.getTitle());

				var requests = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
						.connectTimeout(Duration.ofSeconds(10)).build());
				requests.setReadTimeout(Duration.ofSeconds(10));
				var http = RestClient.builder().requestFactory(requests)
						.baseUrl("http://127.0.0.1:" + context.getEnvironment().getRequiredProperty("local.server.port"))
						.build();
				factory.getStatistics().clear();
				httpEvent(http, saved.getId(), "cached title");
				assertThat(factory.getStatistics().getPrepareStatementCount()).isZero();
				System.out.println("CLOUD_HTTP_HIT status=200 SQL=0 title=cached title");

				service.removeEventFromCache(saved.getId());
				assertThat(cache.get(saved.getId())).isNull();
				factory.getStatistics().clear();
				Event reloaded = service.getEvent(saved.getId());
				assertThat(reloaded.getTitle()).isEqualTo("database title");
				assertThat(factory.getStatistics().getPrepareStatementCount()).isPositive();
				System.out.println("CLOUD_SERVICE_RELOAD SQL=" + factory.getStatistics().getPrepareStatementCount());
				CacheRuntime.descriptors(reloaded);
				if (hardened && args[1].equals("Timestamp")) {
					assertThatThrownBy(() -> cache.get(saved.getId())).hasRootCauseInstanceOf(InvalidClassException.class);
					System.out.println("CLOUD_PERMISSION_REJECTED Timestamp");
					nativeCache.clear();
				}
				else {
					factory.getStatistics().clear();
					httpEvent(http, saved.getId(), "database title");
					assertThat(factory.getStatistics().getPrepareStatementCount()).isZero();
					System.out.println("CLOUD_HTTP_REPOPULATED_HIT SQL=0 title=database title");

					var eviction = http.delete().uri("/api/events/{id}", saved.getId()).retrieve().toBodilessEntity();
					assertThat(eviction.getStatusCode().value()).isEqualTo(200);
					assertThat(cache.get(saved.getId())).isNull();
					factory.getStatistics().clear();
					httpEvent(http, saved.getId(), "database title");
					assertThat(factory.getStatistics().getPrepareStatementCount()).isPositive();
					System.out.println("CLOUD_HTTP_RELOAD status=200 SQL=" + factory.getStatistics().getPrepareStatementCount());
					factory.getStatistics().clear();
					assertThat(service.getEvent(saved.getId()).getTitle()).isEqualTo("database title");
					assertThat(factory.getStatistics().getPrepareStatementCount()).isZero();
					System.out.println("CLOUD_SERVICE_REPOPULATED_HIT SQL=0 title=database title");
					service.removeEventFromCache(saved.getId());
				}
			}
			assertThat(cache.get(saved.getId())).isNull();
			assertThat(context.getBean(EventRepository.class).findById(saved.getId()).orElseThrow().getTitle()).isEqualTo("database title");
			System.out.println("CLOUD_CACHE_PATH_PASS scenario=" + args[1] + " hardened=" + hardened);
		}
		finally {
			Coherence.closeAll();
			CacheFactory.shutdown();
		}
	}

	private static void httpEvent(RestClient http, Long id, String title) {
		var response = http.get().uri("/api/events/{id}", id).retrieve().toEntity(Event.class);
		assertThat(response.getStatusCode().value()).isEqualTo(200);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getId()).isEqualTo(id);
		assertThat(response.getBody().getTitle()).isEqualTo(title);
	}

	private static void policy(String scenario) throws Exception {
		Class<?> config = Class.forName("com.tangosol.internal.util.security.SecurityConfig");
		Object policy = config.getMethod("current").invoke(null);
		Class<?> executable = Class.forName("com.tangosol.util.RemoteExecutablePolicy");
		Object executablePolicy = executable.getMethod("current").invoke(null);
		for (String name : List.of(Event.class.getName(), "org.hibernate.collection.spi.PersistentSet",
				"org.hibernate.collection.spi.AbstractPersistentCollection", "java.sql.Timestamp")) {
			boolean permitted = !name.endsWith("." + scenario);
			assertThat(config.getMethod("contains", String.class).invoke(policy, name)).isEqualTo(permitted);
			assertThat(config.getMethod("isExecutable", String.class).invoke(policy, name)).isEqualTo(false);
			assertThat(executable.getMethod("isExecutable", Class.class).invoke(executablePolicy, Class.forName(name))).isEqualTo(false);
			if (permitted) {
				assertThat(config.getMethod("source", String.class).invoke(policy, name)).isEqualTo("manual");
			}
		}
		assertThat(config.getMethod("contains", String.class).invoke(policy, Person.class.getName())).isEqualTo(false);
		var serializer = new DefaultSerializer();
		var binary = ExternalizableHelper.toBinary(new Person(), serializer);
		assertThatThrownBy(() -> ExternalizableHelper.fromBinary(binary, serializer)).hasRootCauseInstanceOf(InvalidClassException.class);
	}
}
