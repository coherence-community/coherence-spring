/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.serialization.pof;

import java.io.IOException;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofSerializer;
import com.tangosol.io.pof.PofWriter;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class GrantedAuthorityPofSerializer implements PofSerializer {
	@Override
	public void serialize(PofWriter pofWriter, Object value) throws IOException {
		final SimpleGrantedAuthority grantedAuthority = (SimpleGrantedAuthority) value;
		pofWriter.writeString(0, grantedAuthority.getAuthority());
		pofWriter.writeRemainder(null);
	}

	@Override
	public Object deserialize(PofReader pofReader) throws IOException {
		final String authorityValue = pofReader.readString(0);
		pofReader.readRemainder();
		final SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityValue);
		return authority;
	}
}
