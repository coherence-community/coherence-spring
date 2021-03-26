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

@LargePalmTrees                                                              // <1>
@Component                                                                   // <2>
public class LargePalmTreesFilterFactory<Plant>
		implements FilterFactory<LargePalmTrees, Plant> {
	@Override
	public Filter<Plant> create(LargePalmTrees annotation) {                 // <3>
		Filter<Plant> palm = Filters.equal("plantType", PlantType.PALM);
		Filter<Plant> height = Filters.greaterEqual(
				Extractors.extract("height"), 20);
		return Filters.all(palm, height);
	}
}
