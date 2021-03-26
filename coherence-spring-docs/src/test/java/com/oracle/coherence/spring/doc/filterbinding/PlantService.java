// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.filterbinding;

import javax.annotation.Resource;

import com.oracle.coherence.spring.annotation.Name;
import com.oracle.coherence.spring.annotation.View;
import com.tangosol.net.NamedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class PlantService {
	private final Log logger = LogFactory.getLog(PlantService.class);

	@View                                                                    // <1>
	@LargePalmTrees                                                          // <2>
	@Name("plants")                                                          // <3>
	@Resource(name = "getCache")                                             // <4>
	private NamedMap<Long, Plant> largePalmTrees;
// end::hide[]
	@View                                                                    // <1>
	@PalmTrees(1)                                                            // <2>
	@Name("plants")                                                          // <3>
	@Resource(name = "getCache")                                             // <4>
	private NamedMap<Long, Plant> palmTrees;
// tag::hide[]
	public void getLargePalmTrees() {
		assertThat(this.largePalmTrees.size()).isEqualTo(2);
		assertThat(this.largePalmTrees.values()).extracting(Plant::getHeight).containsExactlyInAnyOrder(22, 25);
		assertThat(this.largePalmTrees.values()).extracting(Plant::getName).containsExactlyInAnyOrder("Coconut", "Date Palm");
	}

	public void getPalmTrees() {
		assertThat(this.palmTrees.size()).isEqualTo(3);
		assertThat(this.palmTrees.values()).extracting(Plant::getHeight).containsExactlyInAnyOrder(2, 22, 25);
		assertThat(this.palmTrees.values()).extracting(Plant::getName).containsExactlyInAnyOrder("Coconut", "Date Palm", "Dwarf palmetto");
	}
}
// end::hide[]