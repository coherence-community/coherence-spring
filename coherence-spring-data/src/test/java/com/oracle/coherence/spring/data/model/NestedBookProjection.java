package com.oracle.coherence.spring.data.model;

import java.io.Serializable;

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
