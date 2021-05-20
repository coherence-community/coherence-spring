/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class AuthenticationPofSerializer implements PofSerializer {
	@Override
	public void serialize(PofWriter pofWriter, Object value) throws IOException {
		final UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) value;
		pofWriter.writeCollection(1, authentication.getAuthorities(), SimpleGrantedAuthority.class);
		pofWriter.writeString(2, authentication.getCredentials().toString());
		pofWriter.writeObject(3, authentication.getDetails());
		pofWriter.writeObject(4, authentication.getPrincipal());
		pofWriter.writeRemainder(null);
	}

	@Override
	public Object deserialize(PofReader pofReader) throws IOException {

		final Collection<SimpleGrantedAuthority> authorities = pofReader.readCollection(1, new ArrayList<SimpleGrantedAuthority>());
		final Object credentials = pofReader.readObject(2);
		final Object details = pofReader.readObject(3);
		final Object principal = pofReader.readObject(4);

		pofReader.readRemainder();
		final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, credentials, authorities);

		authentication.setDetails(details);
		return authentication;
	}
}
