/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.coherence.spring.CoherenceContext;
import com.oracle.coherence.spring.example.model.Person;
import com.oracle.coherence.spring.example.model.PersonRepository;
import com.tangosol.io.DefaultSerializer;
import com.tangosol.io.Serializer;
import com.tangosol.net.Coherence;
import com.tangosol.util.ExternalizableHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Checks the sample's exact payload permission and real entity wire graph.
 */
class PersonSerializationTests {

	@Test
	void restoresPersonWithoutAdditionalApplicationTypes() throws Exception {
		Person person = new Person();
		person.setId(42L);
		person.setAge(21);
		person.setFirstname("Ana");
		person.setLastname("Petrovic");
		assertPersonGraph(person);
	}

	@Test
	@EnabledIf("securityPolicyAvailable")
	void permitsOnlyTheSamplePayloadWithoutExecutablePermission() throws Exception {
		assertPolicyBoundary();
	}

	static void assertPersonGraph(Person person) throws Exception {
		assertThat(person.getClass()).isEqualTo(Person.class);
		assertThat(person.getClass().getSuperclass()).isEqualTo(Object.class);
		try (GraphOutputStream out = new GraphOutputStream()) {
			out.writeObject(person);
			assertThat(out.descriptors).containsExactlyInAnyOrder(Person.class.getName(), "java.lang.Long", "java.lang.Number");
			assertThat(out.objects).containsExactlyInAnyOrder(Person.class.getName(), "java.lang.Long", "java.lang.String");
			System.out.println("Person graph: descriptors=" + out.descriptors + "; objects=" + out.objects);
		}
		DefaultSerializer serializer = new DefaultSerializer();
		Person restored = ExternalizableHelper.fromBinary(ExternalizableHelper.toBinary(person, serializer), serializer);
		assertThat(restored.getClass()).isEqualTo(Person.class);
		assertThat(restored.getId()).isEqualTo(person.getId());
		assertThat(restored.getFirstname()).isEqualTo(person.getFirstname());
		assertThat(restored.getLastname()).isEqualTo(person.getLastname());
		assertThat(restored.getAge()).isEqualTo(person.getAge());
	}

	static void assertEndpoint(Serializer serializer) throws Exception {
		assertThat(serializer).isInstanceOf(DefaultSerializer.class);
		if (securityPolicyAvailable()) {
			assertPolicyBoundary();
			assertThat(Class.forName("com.tangosol.util.CoherenceMode")
					.getMethod("isSecurityHardeningEnabled").invoke(null)).isEqualTo(true);
		}
		System.out.println("Person endpoint: java=" + System.getProperty("java.runtime.version")
				+ "; mode=" + System.getProperty("coherence.security.mode") + "; serializer=" + serializer
				+ "; model=" + Person.class.getProtectionDomain().getCodeSource().getLocation());
	}

	private static void assertPolicyBoundary() throws Exception {
		Class<?> config = Class.forName("com.tangosol.internal.util.security.SecurityConfig");
		Object policy = config.getMethod("current").invoke(null);
		assertThat(config.getMethod("contains", String.class).invoke(policy, Person.class.getName())).isEqualTo(true);
		assertThat(config.getMethod("source", String.class).invoke(policy, Person.class.getName())).isEqualTo("manual");
		assertThat(config.getMethod("isExecutable", String.class).invoke(policy, Person.class.getName())).isEqualTo(false);
		Class<?> executablePolicy = Class.forName("com.tangosol.util.RemoteExecutablePolicy");
		Object remotePolicy = executablePolicy.getMethod("current").invoke(null);
		assertThat(executablePolicy.getMethod("isExecutable", Class.class).invoke(remotePolicy, Person.class))
				.isEqualTo(false);
		Class<?> allowlist = Class.forName("com.tangosol.io.internal.SerializationAllowlist");
		assertThat(allowlist.getMethod("isAllowlisted", Class.class).invoke(null, Person.class)).isEqualTo(true);
		assertThat(allowlist.getMethod("isAllowlisted", Class.class).invoke(null, UnlistedPayload.class)).isEqualTo(false);
		UnlistedPayload.readCount = 0;
		DefaultSerializer serializer = new DefaultSerializer();
		var binary = ExternalizableHelper.toBinary(new UnlistedPayload(), serializer);
		assertThatThrownBy(() -> ExternalizableHelper.fromBinary(binary, serializer))
				.hasRootCauseInstanceOf(InvalidClassException.class);
		assertThat(UnlistedPayload.readCount).isZero();
	}

	static boolean securityPolicyAvailable() {
		try {
			Class.forName("com.tangosol.io.internal.SerializationAllowlist");
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

	static final class ServerProbe implements RemoteCallable<String> {

		private static final long serialVersionUID = 1L;

		private final Long id;

		ServerProbe(Long id) {
			this.id = id;
		}

		@Override
		public String call() throws Exception {
			Serializer serializer = Coherence.getInstance().getSession().getCache("people").getCacheService().getSerializer();
			assertEndpoint(serializer);
			if (this.id != null) {
				PersonRepository repository = CoherenceContext.getApplicationContext().getBean(PersonRepository.class);
				assertPersonGraph(repository.load(this.id));
			}
			return System.getProperty("coherence.security.mode", "unset");
		}
	}

	private static final class GraphOutputStream extends ObjectOutputStream {

		private final Set<String> descriptors = new TreeSet<>();

		private final Set<String> objects = new TreeSet<>();

		GraphOutputStream() throws IOException {
			super(new ByteArrayOutputStream());
			enableReplaceObject(true);
		}

		@Override
		protected void annotateClass(Class<?> type) {
			this.descriptors.add(type.getName());
		}

		@Override
		protected Object replaceObject(Object value) {
			this.objects.add(value.getClass().getName());
			return value;
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
