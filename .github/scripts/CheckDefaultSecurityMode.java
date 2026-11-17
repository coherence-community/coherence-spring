/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import com.tangosol.io.DefaultSerializer;
import com.tangosol.net.CacheFactory;
import com.tangosol.util.CoherenceMode;
import com.tangosol.util.ExternalizableHelper;

/** Checks the released 26.10 default in a fresh JVM without a mode override. */
public final class CheckDefaultSecurityMode {

	public static void main(String[] args) throws Exception {
		if (System.getProperty("coherence.security.mode") != null
				|| System.getenv("COHERENCE_SECURITY_MODE") != null
				|| System.getenv("TANGOSOL_COHERENCE_SECURITY_MODE") != null) {
			throw new AssertionError("Default-mode check requires absent mode overrides");
		}
		if (!"26.10".equals(CacheFactory.VERSION)) {
			throw new AssertionError("Expected released Coherence 26.10, found " + CacheFactory.VERSION);
		}
		if (!CoherenceMode.isSecurityHardeningEnabled()) {
			throw new AssertionError("Hardening is disabled with no mode override");
		}
		DefaultSerializer serializer = new DefaultSerializer(CheckDefaultSecurityMode.class.getClassLoader());
		var binary = ExternalizableHelper.toBinary(new UnpermittedPayload(), serializer);
		try {
			ExternalizableHelper.fromBinary(binary, serializer);
			throw new AssertionError("Unpermitted payload was accepted");
		}
		catch (RuntimeException rejected) {
			Throwable cause = rejected;
			while (cause.getCause() != null && cause.getCause() != cause) {
				cause = cause.getCause();
			}
			if (!(cause instanceof InvalidClassException)) {
				throw new AssertionError("Unexpected deserialization failure", rejected);
			}
		}
		if (UnpermittedPayload.readCount != 0) {
			throw new AssertionError("Payload callback ran before rejection");
		}
		System.out.println("DEFAULT_HARDENING_PASS coherence=" + CacheFactory.VERSION + " java="
				+ System.getProperty("java.runtime.version") + " home=" + System.getProperty("java.home")
				+ " jar=" + CoherenceMode.class.getProtectionDomain().getCodeSource().getLocation()
				+ " mode=<absent> hardened=true payload=rejected callback=false");
	}

	private static final class UnpermittedPayload implements Serializable {

		private static final long serialVersionUID = 1L;

		private static int readCount;

		private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
			readCount++;
			input.defaultReadObject();
		}

	}

}
