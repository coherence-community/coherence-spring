package com.oracle.coherence.spring.example;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.coherence.CoherenceClusterMember;
import com.oracle.bedrock.runtime.coherence.callables.IsServiceRunning;
import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.testsupport.MavenProjectFileUtils;
import com.oracle.bedrock.testsupport.deferred.Eventually;
import com.oracle.bedrock.testsupport.junit.TestLogsExtension;
import com.oracle.coherence.spring.example.model.Person;
import com.oracle.coherence.spring.example.model.PersonRepository;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.SessionConfiguration;
import org.hsqldb.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This test starts HSQLDB in an external process and then starts the {@link CacheStoreDemo}
 * Spring Boot application in another external process. The test then starts a Coherence Extend
 * client session that connects to the {@link Person} cache and performs puts and gets on the cache.
 * The test then verifies the state of the database to assert that the JPA cache store worked correctly.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(SpringExtension.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, })
public class CacheStoreDemoIT {

	/**
	 * Repository bean injected by Spring.
	 */
	@Autowired
	private PersonRepository repository;

	/**
	 * The HSQLDB process started by Bedrock.
	 */
    static JavaApplication hsqldb;

	/**
	 * The {@link CacheStoreDemo} application started by Bedrock.
	 */
	static CoherenceClusterMember server;

	/**
	 * The Coherence {@link Session} to use for getting test caches.
	 */
	private static Session session;

	/**
	 * A JUnit5 extension to capture logs from processes started by Bedrock.
	 */
    @RegisterExtension
    static TestLogsExtension testLogs = new TestLogsExtension(CacheStoreDemoIT.class);

	/**
	 * Field used to ensure that different tests use different IDs for departments.
	 */
	static AtomicLong id = new AtomicLong(1L);

	@BeforeAll
	static void setup() throws Exception {
		File outputDir = MavenProjectFileUtils.ensureTestOutputFolder(CacheStoreDemoIT.class, null);
		File dbDir = new File(outputDir, "hsqldb");

		// clean-up any previous database files from old tests
		MavenProjectFileUtils.recursiveDelete(dbDir);
		// re-create the db folder
		assertThat(dbDir.mkdirs(), is(true));
		assertThat(dbDir.exists(), is(true));
		assertThat(dbDir.isDirectory(), is(true));

		// Run db processes on the local machine
		LocalPlatform platform = LocalPlatform.get();

		testLogs.init(CacheStoreDemoIT.class, "logs");

		// Start the HSQLDB listening on port 9001
		hsqldb = platform.launch(JavaApplication.class,
				ClassName.of(Server.class),
				WorkingDirectory.at(dbDir),
				testLogs,
				ClassPath.ofClass(Server.class),
				Arguments.of("--database.0", "file:testdb", "--dbname.0", "testdb", "--port", 9001),
				DisplayName.of("HSQLDB"));

		// Start the Coherence demo server
		server = platform.launch(CoherenceClusterMember.class,
				ClassName.of(CacheStoreDemo.class),
				testLogs,
				SystemProperty.of("spring.jmx.enabled", true),
				DisplayName.of("server"));

		// Wait for Spring to start
		Eventually.assertDeferred(() -> server.invoke(IsSpringUp.INSTANCE), is(true));
		// Ensure the Coherence Extend proxy has started
		Eventually.assertDeferred(() -> server.invoke(new IsServiceRunning("Proxy")), is(true));

		// Create the local Coherence Extend client
		CoherenceConfiguration config = CoherenceConfiguration.builder()
				.withSession(SessionConfiguration.create("client-cache-config.xml")).build();
		Coherence coherence = Coherence.client(config);
		// wait at most 1 minute for the Coherence DefaultCacheServer to start
		// DefaultCacheServer is started automatically by the application but for the
		// tests to pass we need to ensure it has finished starting cache services.
		coherence.start().get(1, TimeUnit.MINUTES);

		// Create the Coherence Session using the client cache configuration that will connect over Extend.
		session = coherence.getSession();
	}

	@AfterAll
	static void cleanup() {
		if (hsqldb != null) {
			hsqldb.close();
		}
		if (server != null) {
			server.close();
		}
	}

	/**
	 * This test will cause a cache store load which will be a miss as there is no
	 * entry in the db for the key.
	 */
	@Test
	public void shouldNotGetPersonNotInDB() {
		NamedCache<Long, Person> cache = session.getCache("people");
		Person result = cache.get(-1L);
		assertThat(result, is(nullValue()));
	}

	/**
	 * This test loads a Person into the db then gets it from the cache
	 * which will cause a read-through from the db into the cache.
	 */
	@Test
	public void shouldGetPersonInDB() {
		Person person = new Person();
		person.setId(id.getAndIncrement());
		person.setFirstname("Foo");
		person.setLastname("Bar");

		person = repository.saveAndFlush(person);

		NamedCache<Long, Person> departments = session.getCache("people");
		Person result = departments.get(person.getId());
		assertThat(result, is(notNullValue()));
		assertThat(result.getFirstname(), is(person.getFirstname()));
		assertThat(result.getLastname(), is(person.getLastname()));
	}

	/**
	 * This test will add a person to the cache which will cause
	 * a write-through to the db.
	 */
	@Test
	public void shouldCreatePerson() {
		Person person = new Person();
		person.setId(id.getAndIncrement());
		person.setFirstname("Foo");
		person.setLastname("Bar");
		person.setAge(21);

		NamedCache<Long, Person> departments = session.getCache("people");
		departments.put(person.getId(), person);

		Optional<Person> byId = repository.findById(person.getId());
		assertThat(byId.isPresent(), is(true));
		Person result = byId.get();
		assertThat(result.getFirstname(), is(person.getFirstname()));
		assertThat(result.getLastname(), is(person.getLastname()));
		assertThat(result.getAge(), is(person.getAge()));
	}

	/**
	 * This test obtain a person from the cache that exists in the db
	 * then put the updated person back into the cache causing a db update.
	 */
	@Test
	public void shouldUpdatePerson() {
		Person person = new Person();
		person.setId(id.getAndIncrement());
		person.setFirstname("Foo");
		person.setLastname("Bar");
		person.setAge(19);

		person = repository.saveAndFlush(person);

		NamedCache<Long, Person> cache = session.getCache("people");
		Person cached = cache.get(person.getId());

		assertThat(cached, is(notNullValue()));
		assertThat(cached.getId(), is(person.getId()));
		assertThat(cached.getFirstname(), is(person.getFirstname()));
		assertThat(cached.getLastname(), is(person.getLastname()));
		assertThat(cached.getAge(), is(19));

		cached.setAge(20);
		cache.put(cached.getId(), cached);

		Optional<Person> byId = repository.findById(person.getId());
		assertThat(byId.isPresent(), is(true));
		Person result = byId.get();
		assertThat(result, is(notNullValue()));
		assertThat(result.getAge(), is(20));
	}

}
