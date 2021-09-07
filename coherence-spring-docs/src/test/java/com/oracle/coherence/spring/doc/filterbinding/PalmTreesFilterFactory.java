// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.filterbinding;
// end::hide[]

import com.oracle.coherence.spring.annotation.FilterFactory;
import com.tangosol.util.Extractors;
import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import org.springframework.stereotype.Component;

@PalmTrees                                                                // <1>
@Component                                                                // <2>
public class PalmTreesFilterFactory<Plant>
        implements FilterFactory<PalmTrees, Plant> {
    @Override
    public Filter<Plant> create(PalmTrees annotation) {                   // <3>
        Filter<Plant> palm = Filters.equal("plantType", PlantType.PALM);
        Filter<Plant> height = Filters.greaterEqual(
                Extractors.extract("height"), annotation.value());  // <4>
        return Filters.all(palm, height);
    }
}
