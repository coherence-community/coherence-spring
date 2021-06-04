/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.model;

/**
 * Example of closed interface projection
 */
public interface BookProjection {
	String getTitle();

	int getPages();

	CharSequence getTitleSequence();

	AuthorSummary getAuthor();

	interface AuthorSummary {
		String getFirstName();

		AddressProjection getAddress();
	}

	interface AddressProjection {
		String getStreet();
		String getNumber();
	}
}
