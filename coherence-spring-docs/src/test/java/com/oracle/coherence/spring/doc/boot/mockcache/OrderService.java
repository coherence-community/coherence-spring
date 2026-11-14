/*
 * Copyright (c) 2013, 2026, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
// tag::example[]
package com.oracle.coherence.spring.doc.boot.mockcache;

import com.oracle.coherence.spring.configuration.annotation.CoherenceMap;
import com.tangosol.net.NamedCache;

import org.springframework.stereotype.Service;

@Service
public class OrderService {

	private final NamedCache<Long, String> orders;

	public OrderService(@CoherenceMap("orders") NamedCache<Long, String> orders) {
		this.orders = orders;
	}

	public String getStatus(long orderId) {
		return this.orders.get(orderId);
	}

}
// end::example[]
