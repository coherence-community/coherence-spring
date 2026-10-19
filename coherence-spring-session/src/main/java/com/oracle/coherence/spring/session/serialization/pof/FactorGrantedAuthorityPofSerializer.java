/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.serialization.pof;

import java.io.IOException;
import java.time.Instant;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

import org.springframework.security.core.authority.FactorGrantedAuthority;

/**
 * Preserves the authority and issuance time added by Spring Security 7 authentication.
 *
 * @author Vaso Putica
 * @since 5.0
 */
public class FactorGrantedAuthorityPofSerializer implements PofSerializer<FactorGrantedAuthority> {

	@Override
	public void serialize(PofWriter writer, FactorGrantedAuthority authority) throws IOException {
		writer.writeString(0, authority.getAuthority());
		writer.writeObject(1, authority.getIssuedAt());
		writer.writeRemainder(null);
	}

	@Override
	public FactorGrantedAuthority deserialize(PofReader reader) throws IOException {
		String authority = reader.readString(0);
		Instant issuedAt = reader.readObject(1);
		reader.readRemainder();
		return FactorGrantedAuthority.withAuthority(authority).issuedAt(issuedAt).build();
	}
}
