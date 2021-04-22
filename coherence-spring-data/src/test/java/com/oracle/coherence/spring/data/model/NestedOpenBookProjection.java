package com.oracle.coherence.spring.data.model;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

public interface NestedOpenBookProjection {
	String getTitle();

	int getPages();

	AuthorSummary getAuthor();

	interface AuthorSummary {
		String getFirstName();

		AddressProjection getAddress();

		// nested open projection
		@Value("#{target.firstName + ' ' + target.lastName}")
		Optional<String> getFullName();

		interface AddressProjection {
			String getStreet();
			String getNumber();
		}
	}
}
