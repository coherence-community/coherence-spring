/*
 * File: StubBackingMapListener.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.coherence.spring;

import com.tangosol.net.BackingMapManagerContext;

import com.tangosol.util.MapEvent;
import com.tangosol.util.MultiplexingMapListener;

/**
 * Stub backing map listener implementation for testing by
 * {@link SpringNamespaceHandlerTests}.
 *
 * @author Patrick Peralta
 */
public class StubBackingMapListener extends MultiplexingMapListener
{
    private volatile boolean         m_fCtxConfigured = false;
    private BackingMapManagerContext m_ctx;


    /**
     * Construct a StubBackingMapListener.
     *
     */
    public StubBackingMapListener()
    {
        super();
    }


    /**
     * Return the BackingMapManagerContext.
     *
     * @return the BackingMapManagerContext
     */
    public BackingMapManagerContext getBackingMapManagerContext()
    {
        return m_ctx;
    }


    /**
     * Set the BackingMapManagerContext.
     *
     * @param ctx the BackingMapManagerContext
     */
    public void setBackingMapManagerContext(BackingMapManagerContext ctx)
    {
        m_ctx = ctx;
    }


    /**
     * Return true if the BackingMapManagerContext was set via {@link #setBackingMapManagerContext}.
     *
     * @return true if the BackingMapManagerContext was set
     */
    public boolean isContextConfigured()
    {
        return m_fCtxConfigured;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMapEvent(MapEvent evt)
    {
        m_fCtxConfigured = m_ctx != null;
    }
}
