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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;

public class SecurityContextPofSerializer implements PofSerializer {
	@Override
	public void serialize(PofWriter pofWriter, Object value) throws IOException {
		final SecurityContextImpl securityContext = (SecurityContextImpl) value;
		pofWriter.writeObject(0, securityContext.getAuthentication());
		pofWriter.writeRemainder(null);
	}

	@Override
	public Object deserialize(PofReader pofReader) throws IOException {
		final Authentication authentication = pofReader.readObject(0);
		pofReader.readRemainder();
		final SecurityContextImpl securityContext = new SecurityContextImpl(authentication);
		return securityContext;
	}
}
