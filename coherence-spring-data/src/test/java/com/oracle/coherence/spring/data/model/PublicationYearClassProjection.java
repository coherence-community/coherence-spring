/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.data.model;

import java.time.LocalDate;

/**
 * Example of class based projection.
 * @author Vaso Putica
 * @author Gunnar Hillert
 */
public class PublicationYearClassProjection {
	private final int publicationYear;
	private final LocalDate published;

	public PublicationYearClassProjection(int publicationYear, LocalDate published) {
		this.publicationYear = publicationYear;
		this.published = published;
	}

	public int getPublicationYear() {
		return this.publicationYear;
	}

	public LocalDate getPublished() {
		return this.published;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		PublicationYearClassProjection that = (PublicationYearClassProjection) o;

		if (getPublicationYear() != that.getPublicationYear()) {
			return false;
		}
		return (getPublished() != null) ? getPublished().equals(that.getPublished()) : that.getPublished() == null;
	}

	@Override
	public int hashCode() {
		int result = getPublicationYear();
		result = 31 * result + ((getPublished() != null) ? getPublished().hashCode() : 0);
		return result;
	}
}
