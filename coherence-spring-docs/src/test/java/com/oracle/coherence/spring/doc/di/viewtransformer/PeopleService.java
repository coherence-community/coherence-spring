// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.di.viewtransformer;

import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.View;
import com.oracle.coherence.spring.configuration.annotation.CoherenceMap;
import com.oracle.coherence.spring.doc.extractorbinding.PlantNameExtractor;
import com.tangosol.net.NamedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.assertj.core.api.Assertions;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class PeopleService {
	private final Log logger = LogFactory.getLog(PeopleService.class);
// end::hide[]
	@CoherenceMap("people")                          // <1>
	@View                                            // <2>
	@PropertyExtractor("age")                        // <3>
	private NamedMap<String, Integer> ages;          // <4>
// tag::hide[]
	public void getPeople() {
		assertThat(this.ages.size()).isEqualTo(5);
		Assertions.assertThat(this.ages.values()).containsExactly(
				50, 36, 32, 40, 25);
	}
}
// end::hide[]