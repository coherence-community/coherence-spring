package com.oracle.coherence.spring;

import com.tangosol.net.events.EventInterceptor;
import com.tangosol.net.events.partition.cache.EntryEvent;

/**
 * Stub Interceptor implementation for testing by {@link SpringNamespaceHandlerTests}.
 *
 * @author pp  2012.02.08
 */
public class StubInterceptor
        implements EventInterceptor<EntryEvent>
    {
    @Override
    public void onEvent(EntryEvent event)
        {
        m_fEventReceived = true;
        }

    public static boolean eventReceived()
        {
        return m_fEventReceived;
        }

    private static volatile boolean m_fEventReceived = false;
    }