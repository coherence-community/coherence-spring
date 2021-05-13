/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.model;

import java.io.Serializable;

/**
 * Example of nested projection
 */
public interface NestedBookProjection {
	AuthorSummary getAuthor();

	String getTitle();

	int getPages();

	interface AuthorSummary {
		String getFirstName();

		AddressProjection getAddress();

		Serializable getUpperFirstName();

		interface AddressProjection {
			String getStreet();
			String getNumber();
		}
	}
}
