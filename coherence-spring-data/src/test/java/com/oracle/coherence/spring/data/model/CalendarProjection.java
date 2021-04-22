package com.oracle.coherence.spring.data.model;

import java.util.Calendar;

public class CalendarProjection {
	private final int publicationYear;
	private final Calendar published;

	public CalendarProjection(int publicationYear, Calendar published) {
		this.publicationYear = publicationYear;
		this.published = published;
	}

	public int getPublicationYear() {
		return this.publicationYear;
	}

	public Calendar getPublished() {
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

		CalendarProjection that = (CalendarProjection) o;

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
