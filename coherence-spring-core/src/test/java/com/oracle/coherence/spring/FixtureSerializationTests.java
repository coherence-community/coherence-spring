/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.util.ExternalizableHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests fixture graph permissions without allowing arbitrary application payloads.
 */
class FixtureSerializationTests {

	@Test
	void restoresTheCompletePersonGraph() {
		data.PhoneNumber phone = new data.PhoneNumber(381, "1234567");
		data.Person person = new data.Person("Ana", "Petrovic", java.time.LocalDate.of(1990, 1, 2), phone);
		DefaultSerializer serializer = new DefaultSerializer();
		data.Person restored = ExternalizableHelper.fromBinary(ExternalizableHelper.toBinary(person, serializer), serializer);
		assertThat(restored).isEqualTo(person);
		assertThat(restored.getPhoneNumber()).isEqualTo(phone);
	}

	@Test
	@EnabledIf("securityPolicyAvailable")
	void rejectsUnlistedPayloadBeforeItsReadCallback() throws Exception {
		UnlistedPayload.readCount = 0;
		Class<?> allowlist = Class.forName("com.tangosol.io.internal.SerializationAllowlist");
		assertThat(allowlist.getMethod("isAllowlisted", Class.class).invoke(null, UnlistedPayload.class)).isEqualTo(false);
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

	private static final class UnlistedPayload implements Serializable {

		private static final long serialVersionUID = 1L;

		private static int readCount;

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			readCount++;
		}
	}
}
