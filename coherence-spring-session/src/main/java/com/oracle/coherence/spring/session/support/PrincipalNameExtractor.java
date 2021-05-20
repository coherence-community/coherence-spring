/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.support;

import com.oracle.coherence.spring.session.CoherenceIndexedSessionRepository;
import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;
import com.tangosol.util.extractor.AbstractExtractor;

import org.springframework.session.MapSession;

/**
 * ValueExtractor that returns the name of the Spring Security principal from the {@link MapSession}.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
public class PrincipalNameExtractor extends AbstractExtractor<MapSession, String> implements PortableObject {

	@Override
	public String extract(MapSession session) {
		return session.getAttribute(CoherenceIndexedSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
	}

	@Override
	public void readExternal(PofReader in) {
	}

	@Override
	public void writeExternal(PofWriter out) {
	}
}
