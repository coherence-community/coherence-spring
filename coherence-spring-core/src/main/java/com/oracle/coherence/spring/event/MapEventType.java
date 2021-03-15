/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.event;

/**
 * Event type enumeration.
 *
 * @author Gunnar Hillert
 * @since 3.0
 * @see com.tangosol.util.MapEvent
 */
public enum MapEventType {

	/**
	 * This event indicates that an entry has been added to the map.
	 */
	INSERTED,

	/**
	 * This event indicates that an entry has been updated in the map.
	 */
	UPDATED,

	/**
	 * This event indicates that an entry has been removed from the map.
	 */
	DELETED
}
