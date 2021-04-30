/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import com.tangosol.io.pof.PofReader;
import com.tangosol.io.pof.PofWriter;
import com.tangosol.io.pof.PortableObject;

public class PhoneNumber
		implements PortableObject, Serializable {

	private int countryCode;

	private String number;

	/**
	 * Default constructor for serialization.
	 */
	public PhoneNumber() {
	}

	/**
	 * Create a {@link PhoneNumber}.
	 *
	 * @param countryCode the country code
	 * @param number      the phone number
	 */
	public PhoneNumber(int countryCode, String number) {
		this.countryCode = countryCode;
		this.number = number;
	}

	public int getCountryCode() {
		return this.countryCode;
	}

	public void setCountryCode(int countryCode) {
		this.countryCode = countryCode;
	}

	public String getNumber() {
		return this.number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PhoneNumber that = (PhoneNumber) o;
		return Objects.equals(this.countryCode, that.countryCode)
				&& Objects.equals(this.number, that.number);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.countryCode, this.number);
	}

	@Override
	public String toString() {
		return "{countryCode: " + this.countryCode
				+ ", number: \"" + this.number + "\"}";
	}

	@Override
	public void readExternal(PofReader in) throws IOException {
		this.countryCode = in.readInt(0);
		this.number = in.readString(1);
	}

	@Override
	public void writeExternal(PofWriter out) throws IOException {
		out.writeInt(0, this.countryCode);
		out.writeString(1, this.number);
	}
}
