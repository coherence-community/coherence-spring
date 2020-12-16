/*
 * Copyright (c) 2013, 2020, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring;

import com.tangosol.net.events.EventInterceptor;

import com.tangosol.net.events.partition.cache.EntryEvent;

/**
 * Stub Interceptor implementation for testing by {@link SpringNamespaceHandlerTests}.
 *
 * @author Patrick Peralta
 */
public class StubInterceptor implements EventInterceptor<EntryEvent<?, ?>>
{
	private volatile boolean m_fEventReceived = false;


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onEvent(EntryEvent<?, ?> event)
	{
		m_fEventReceived = true;
	}


	/**
	 * Return true if an event was received via {@link #onEvent}.
	 *
	 * @return true if an event was received.
	 */
	public boolean eventReceived()
	{
		return m_fEventReceived;
	}
}
