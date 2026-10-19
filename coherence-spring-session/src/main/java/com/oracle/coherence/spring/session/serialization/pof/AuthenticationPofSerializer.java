/*
 * Copyright (c) 2021, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.serialization.pof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Serializes username/password tokens with heterogeneous registered authorities,
 * string or null credentials, and application-owned principal/details objects.
 * Index 5 records authentication state (1 for authenticated, -1 otherwise);
 * older data without this field retains the historical authenticated state.
 *
 * @author Gunnar Hillert
 */
public class AuthenticationPofSerializer implements PofSerializer {
	@Override
	public void serialize(PofWriter pofWriter, Object value) throws IOException {
		final UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) value;
		pofWriter.writeCollection(1, authentication.getAuthorities());
		Object credentials = authentication.getCredentials();
		pofWriter.writeString(2, (credentials != null) ? credentials.toString() : null);
		pofWriter.writeObject(3, authentication.getDetails());
		pofWriter.writeObject(4, authentication.getPrincipal());
		pofWriter.writeInt(5, authentication.isAuthenticated() ? 1 : -1);
		pofWriter.writeRemainder(null);
	}

	@Override
	public Object deserialize(PofReader pofReader) throws IOException {

		final Collection<GrantedAuthority> authorities = pofReader.readCollection(1, new ArrayList<GrantedAuthority>());
		final Object credentials = pofReader.readObject(2);
		final Object details = pofReader.readObject(3);
		final Object principal = pofReader.readObject(4);
		final int authenticated = pofReader.readInt(5);

		pofReader.readRemainder();
		final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, credentials, authorities);

		authentication.setDetails(details);
		if (authenticated < 0) {
			authentication.setAuthenticated(false);
		}
		return authentication;
	}
}
