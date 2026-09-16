/*
 * Copyright (c) 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.circuitbreaker;

import com.oracle.coherence.spring.samples.circuitbreaker.controller.BookController;
import com.oracle.coherence.spring.samples.circuitbreaker.service.BookService;
import org.junit.jupiter.api.Test;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies request parameter binding for the sample's book endpoints.
 */
class BookControllerTests {

	@Test
	void bindsBookIdForRetrievalAndEviction() throws Exception {
		final BookService service = mock(BookService.class);
		final BookController controller = new BookController();
		ReflectionTestUtils.setField(controller, "bookService", service);
		final MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

		mvc.perform(get("/api/books/1")).andExpect(status().isOk());
		verify(service).getBook(1L);
		mvc.perform(delete("/api/books/1")).andExpect(status().isOk());
		verify(service).removeBookFromCache(1L);
	}
}
