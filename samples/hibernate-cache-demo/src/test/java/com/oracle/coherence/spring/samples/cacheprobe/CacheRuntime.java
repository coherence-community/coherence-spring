/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.cacheprobe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.ExternalizableHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Observes the effective gates in each JVM; no test security metadata is used. */
final class CacheRuntime {

	private CacheRuntime() {
	}

	static boolean observe() throws Exception {
		boolean hardened = false;
		try {
			Class<?> mode = Class.forName("com.tangosol.util.CoherenceMode");
			hardened = (boolean) mode.getMethod("isSecurityHardeningEnabled").invoke(null);
			assertThat(hardened).isTrue();
			Class<?> policy = Class.forName("com.tangosol.io.internal.SerializationAllowlist");
			assertThat(policy.getMethod("isAllowlisted", Class.class).invoke(null, UnlistedPayload.class)).isEqualTo(false);
			DefaultSerializer serializer = new DefaultSerializer();
			var binary = ExternalizableHelper.toBinary(new UnlistedPayload(), serializer);
			assertThatThrownBy(() -> ExternalizableHelper.fromBinary(binary, serializer))
					.hasRootCauseInstanceOf(InvalidClassException.class);
			assertThat(UnlistedPayload.readCount).isZero();
		}
		catch (ClassNotFoundException ex) {
			assertThat(ex.getMessage()).isEqualTo("com.tangosol.util.CoherenceMode");
		}
		System.out.println("CACHE_RUNTIME java=" + System.getProperty("java.runtime.version") + " home="
				+ System.getProperty("java.home") + " coherence=" + CacheFactory.VERSION + " hardened=" + hardened
				+ " mode=" + System.getProperty("coherence.security.mode") + " pid=" + ProcessHandle.current().pid());
		return hardened;
	}

	static void descriptors(Object value) throws Exception {
		try (ObjectOutputStream output = new ObjectOutputStream(new ByteArrayOutputStream()) {
			@Override
			protected void annotateClass(Class<?> type) {
				System.out.println("CACHE_DESCRIPTOR " + type.getName());
			}
		}) {
			output.writeObject(value);
		}
	}

	private static final class UnlistedPayload implements Serializable {

		private static final long serialVersionUID = 1L;

		private static int readCount;

		private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
			input.defaultReadObject();
			readCount++;
		}
	}
}
