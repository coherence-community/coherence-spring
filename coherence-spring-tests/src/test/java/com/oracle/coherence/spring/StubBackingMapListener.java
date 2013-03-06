package com.oracle.coherence.spring;

import com.tangosol.net.BackingMapManagerContext;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MultiplexingMapListener;

/**
 * Stub backing map listener implementation for testing by
 * {@link SpringNamespaceHandlerTests}.
 *
 * @author pp  2012.02.08
 */
public class StubBackingMapListener
        extends MultiplexingMapListener
    {
    public StubBackingMapListener()
        {
        super();
        }

    public BackingMapManagerContext getBackingMapManagerContext()
        {
        return m_ctx;
        }

    public void setBackingMapManagerContext(BackingMapManagerContext ctx)
        {
        m_ctx = ctx;
        }

    public boolean isContextConfigured()
        {
        return m_fCtxConfigured;
        }

    @Override
    protected void onMapEvent(MapEvent evt)
        {
        m_fCtxConfigured = m_ctx != null;
        }

    private BackingMapManagerContext m_ctx;

    private boolean m_fCtxConfigured = false;
    }
