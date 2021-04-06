/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.event;

import com.oracle.coherence.spring.annotation.event.Deleted;
import com.oracle.coherence.spring.annotation.event.Inserted;
import com.oracle.coherence.spring.annotation.event.MapName;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.tangosol.util.MapEvent;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.fail;

/**
*
* @author Gunnar Hillert
*
*/
@RestController
@RequestMapping("/api/tasks")
public class TestController {

	private final TestService testService;

	public TestController(TestService testService) {
		this.testService = testService;
	}

	@CoherenceEventListener
	public void broadCastEvents(
			@MapName("tasks") @Inserted @Deleted MapEvent<String, String> event) {
		if (event.isInsert()) {
			this.testService.addEventName("insert");
		}
		else if (event.isDelete()) {
			this.testService.addEventName("delete");
		}
		else {
			fail("Did not expect event: " + event.getKey());
		}
	}

}
