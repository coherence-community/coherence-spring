/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.support;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * A simple {@link HttpSessionListener} implementation that keeps track of the number of created and destroyed sessions.
 *
 * @author Gunnar Hillert
 */
public class MyHttpSessionListener implements HttpSessionListener {

	private AtomicInteger sessionsCreatedCount = new AtomicInteger(0);
	private AtomicInteger sessionsDestroyedCount = new AtomicInteger(0);

	@Override
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		System.out.println("MyHttpSessionListener - Session Created " + httpSessionEvent.getSession().getId());
		this.sessionsCreatedCount.incrementAndGet();
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		System.out.println("MyHttpSessionListener - Session destroyed " + httpSessionEvent.getSession().getId());
		this.sessionsDestroyedCount.incrementAndGet();
	}

	public int getSessionsCreatedCount() {
		return this.sessionsCreatedCount.get();
	}

	public int getSessionsDestroyedCount() {
		return this.sessionsDestroyedCount.get();
	}

	public void reset() {
		this.sessionsCreatedCount.set(0);
		this.sessionsDestroyedCount.set(0);
	}
}
