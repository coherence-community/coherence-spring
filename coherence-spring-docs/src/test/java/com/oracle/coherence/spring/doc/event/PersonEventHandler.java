// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.event;
// end::hide[]

import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.tangosol.util.MapEvent;
// tag::hide[]
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
// end::hide[]
import org.springframework.stereotype.Component;

@Component                                                     // <1>
public class PersonEventHandler {
	// tag::hide[]
	private static final Log logger = LogFactory.getLog(PersonEventHandler.class);
	// end::hide[]

	@CoherenceEventListener                                    // <2>
	public void onNewPerson(@MapName("people")                 // <3>
	                        @Inserted                          // <4>
	                        MapEvent<String, Person> event) {
		// TODO: process the event
		// tag::hide[]
		logger.info("Processing person INSERT event " + event.getNewValue());
		// end::hide[]
	}
}
