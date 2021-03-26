// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.extractorbinding;
// end::hide[]

import com.oracle.coherence.spring.annotation.ExtractorFactory;
import com.tangosol.util.Extractors;
import com.tangosol.util.ValueExtractor;
import org.springframework.stereotype.Component;

@PlantNameExtractor                                                         // <1>
@Component                                                                  // <2>
public class PlantNameExtractorFactory<Plant>
        implements ExtractorFactory<PlantNameExtractor, Plant, String> {
    @Override
    public ValueExtractor<Plant, String> create(PlantNameExtractor annotation) {  // <3>
        return Extractors.extract("name");
    }
}
