/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.security;

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
 * Tests complete documentation payload graphs and the surrounding rejection boundary.
 */
class FixtureSerializationTests {

	@Test
	void restoresTheExamplePayloadGraphs() {
		var viewPerson = roundTrip(new com.oracle.coherence.spring.doc.di.viewtransformer.Person("Ana", 36));
		assertThat(viewPerson.getName()).isEqualTo("Ana");
		assertThat(viewPerson.getAge()).isEqualTo(36);
		var filteredPerson = roundTrip(new com.oracle.coherence.spring.doc.di.wherefilter.Person("Ana", "Petrovic", 36));
		assertThat(filteredPerson.getFirstName()).isEqualTo("Ana");
		assertThat(filteredPerson.getLastName()).isEqualTo("Petrovic");
		assertThat(filteredPerson.getAge()).isEqualTo(36);
		var extractedPlant = roundTrip(new com.oracle.coherence.spring.doc.extractorbinding.Plant("Palm",
				com.oracle.coherence.spring.doc.extractorbinding.PlantType.PALM, 12));
		assertThat(extractedPlant.getName()).isEqualTo("Palm");
		assertThat(extractedPlant.getPlantType())
				.isEqualTo(com.oracle.coherence.spring.doc.extractorbinding.PlantType.PALM);
		assertThat(extractedPlant.getHeight()).isEqualTo(12);
		var filteredPlant = roundTrip(new com.oracle.coherence.spring.doc.filterbinding.Plant("Bamboo",
				com.oracle.coherence.spring.doc.filterbinding.PlantType.BAMBOO, 18));
		assertThat(filteredPlant.getName()).isEqualTo("Bamboo");
		assertThat(filteredPlant.getPlantType())
				.isEqualTo(com.oracle.coherence.spring.doc.filterbinding.PlantType.BAMBOO);
		assertThat(filteredPlant.getHeight()).isEqualTo(18);
	}

	private <T> T roundTrip(T value) {
		DefaultSerializer serializer = new DefaultSerializer();
		return ExternalizableHelper.fromBinary(ExternalizableHelper.toBinary(value, serializer), serializer);
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
