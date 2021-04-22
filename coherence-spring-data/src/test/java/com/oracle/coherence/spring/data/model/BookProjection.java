package com.oracle.coherence.spring.data.model;

import java.io.Serializable;

public interface BookProjection {
	String getTitle();

	int getPages();

	CharSequence getTitleSequence();

	AuthorSummary getAuthor();

	interface AuthorSummary {
		String getFirstName();

		AddressProjection getAddress();

		Serializable getLowerFirstName();
	}

	interface AddressProjection {
		String getStreet();
		String getNumber();
	}
}
