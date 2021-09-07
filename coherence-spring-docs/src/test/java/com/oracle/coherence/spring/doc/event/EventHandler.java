/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.event;

import com.oracle.coherence.spring.annotation.PropertyExtractor;
import com.oracle.coherence.spring.annotation.SessionName;
import com.oracle.coherence.spring.annotation.WhereFilter;
import com.oracle.coherence.spring.annotation.event.*;
import com.oracle.coherence.spring.event.CoherenceEventListener;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.util.MapEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventHandler {

	private static final Log logger = LogFactory.getLog(EventHandler.class);

	@CoherenceEventListener
	public void handleEvent(LifecycleEvent event) {
		logger.info("Processing event: " + event.getType().name());
	}

	// tag::all-caches[]
	@CoherenceEventListener
	public void onEvent(MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::all-caches[]

	// tag::foo-map[]
	@CoherenceEventListener
	public void onFooEvent(@MapName("foo")       // <1>
			               MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::foo-map[]

	// tag::bar-cache[]
	@CoherenceEventListener
	public void onBarEvent(@CacheName("bar")     // <1>
			               MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::bar-cache[]

	// tag::all-services[]
	@CoherenceEventListener
	public void onEventFromAllServices(@MapName("foo")  // <1>
			                    MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::all-services[]

	// tag::storage-service[]
	@CoherenceEventListener
	public void onEventOnStorageService(@MapName("foo")
	                    @ServiceName("Storage")  // <1>
			            MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::storage-service[]

	// tag::storage-service-all-caches[]
	@CoherenceEventListener
	public void onEventFromAllCachesOnStorageService(@ServiceName("Storage")  // <1>
			            MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::storage-service-all-caches[]

	// tag::orders-all-sessions[]
	@CoherenceEventListener
	public void onOrdersEventAllSessions(@MapName("orders")  // <1>
			            MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::orders-all-sessions[]

	// tag::orders-customer-session[]
	@CoherenceEventListener
	public void onOrdersEventInCustomerSession(@MapName("orders")
	                    @SessionName("Customer")             // <1>
			            MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::orders-customer-session[]

	// tag::all-caches-customer-session[]
	@CoherenceEventListener
	public void onEventInAllCachesInCustomerSession(@SessionName("Customer") // <1>
			                    MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::all-caches-customer-session[]

	// tag::on-customer-catalog-orders[]
	@CoherenceEventListener
	public void onCustomerOrders(@SessionName("Customer")    // <1>
	                             @MapName("orders")
			                     MapEvent<String, Order> event) {
		// TODO: process the event
	}

	@CoherenceEventListener
	public void onCatalogOrders(@SessionName("Catalog")      // <2>
	                            @MapName("orders")
			                    MapEvent<String, Order> event) {
		// TODO: process the event
	}
	// end::on-customer-catalog-orders[]

	// tag::on-insert-event[]
	@CoherenceEventListener
	public void onInsertEvent(@MapName("test")
	                    @Inserted                            // <1>
			            MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::on-insert-event[]

	// tag::on-insert-delete-event[]
	@CoherenceEventListener
	public void onInsertAndDeleteEvent(@MapName("test")
	                    @Inserted @Deleted                   // <1>
			            MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::on-insert-delete-event[]

	// tag::on-map-event-test-map[]
	@CoherenceEventListener
	public void onMapEvent(@MapName("test") MapEvent<String, String> event) {
		// TODO: process the event
	}
	// end::on-map-event-test-map[]

	// tag::on-map-event-with-where-filter[]
	@WhereFilter("age >= 18")                    // <1>
	@CoherenceEventListener
	@MapName("people")
	public void onAdult(MapEvent<String, Person> people) {
		// TODO: process event...
	}
	// end::on-map-event-with-where-filter[]

	// tag::on-order-with-property-extractor[]
	@CoherenceEventListener
	@PropertyExtractor("customerId")                         // <1>
	public void onOrder(@MapName("orders")                   // <2>
			            MapEvent<String, String> event) {    // <3>
		// TODO: process event...
	}
	// end::on-order-with-property-extractor[]

	// tag::on-order-with-multiple-property-extractors[]
	@CoherenceEventListener
	@PropertyExtractor("customerId")                              // <1>
	@PropertyExtractor("orderId")
	public void onOrderWithMultiplePropertyExtractors(
			            @Inserted                                 // <2>
	                    @MapName("orders")
			            MapEvent<String, List<Object>> event) {   // <3>
		List list = event.getNewValue();
		String customerId = (String) list.get(0);                 // <4>
		Long orderId = (Long) list.get(1);
		// ...
	}
	// end::on-order-with-multiple-property-extractors[]
}
