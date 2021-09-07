// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.extractorbinding;

import com.oracle.coherence.spring.annotation.View;
import com.oracle.coherence.spring.configuration.annotation.CoherenceMap;
import com.tangosol.net.NamedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.api.Assertions;

import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class PlantService {
	private final Log logger = LogFactory.getLog(PlantService.class);
// end::hide[]
	@View                                        // <1>
	@PlantNameExtractor                          // <2>
	@CoherenceMap("plants")                      // <3>
	private NamedMap<Long, String> plants;       // <4>
// tag::hide[]
	public void getPalmTrees() {
		assertThat(this.plants.size()).isEqualTo(5);
		Assertions.assertThat(this.plants.values()).containsExactlyInAnyOrder(
				"Dwarf palmetto", "Coconut", "Date Palm", "Giant Timber Bamboo", "Cavendish");
	}
}
// end::hide[]