/*
 * Copyright (c) 2024, 2025, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.session.support;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

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
		int cBefore = this.sessionsCreatedCount.get();
		int cAfter = this.sessionsCreatedCount.incrementAndGet();
		System.out.println("MyHttpSessionListener - Session Created " + httpSessionEvent.getSession().getId() + "[cBefore=" + cBefore + ", cAfter=" + cAfter + "]");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		int cBefore = this.sessionsDestroyedCount.get();
		int cAfter = this.sessionsDestroyedCount.incrementAndGet();
		System.out.println("MyHttpSessionListener - Session destroyed " + httpSessionEvent.getSession().getId() + "[cBefore=" + cBefore + ", cAfter=" + cAfter + "]");
	}

	public int getSessionsCreatedCount() {
		return this.sessionsCreatedCount.get();
	}

	public int getSessionsDestroyedCount() {
		return this.sessionsDestroyedCount.get();
	}

	public void reset() {
		this.sessionsCreatedCount = new AtomicInteger(0);
		this.sessionsDestroyedCount = new AtomicInteger(0);
	}
}
