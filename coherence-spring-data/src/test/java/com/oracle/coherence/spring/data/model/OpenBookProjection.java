package com.oracle.coherence.spring.data.model;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;

public interface OpenBookProjection {
	AuthorSummary getAuthor();

	String getTitle();

	int getPages();

	// root level open projection
	@Value("#{'Author: ' + target.author.firstName + ', Title: ' + target.title + ', Pages: ' + target.pages}")
	Optional<String> getBasics();

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
