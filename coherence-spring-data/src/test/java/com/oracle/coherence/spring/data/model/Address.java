package com.oracle.coherence.spring.data.model;

import java.io.Serializable;
import java.util.Objects;

public class Address implements Serializable {
    private String street;
    private String number;

    public Address(String street, String number) {
        this.street = street;
        this.number = number;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
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

        Address address = (Address) o;

        if ((getStreet() != null) ? !getStreet().equals(address.getStreet()) : address.getStreet() != null) {
            return false;
        }
        return (getNumber() != null) ? getNumber().equals(address.getNumber()) : address.getNumber() == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStreet(), getNumber());
    }

    @Override
    public String toString() {
        return "Address{" +
                "street='" + this.street + '\'' +
                ", number='" + this.number + '\'' +
                '}';
    }
}
