/*
 * Copyright (c) 2021, 2023, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.autoconfigure.session;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.session.FlushMode;
import org.springframework.session.SaveMode;
import org.springframework.validation.annotation.Validated;

/**
 * Coherence configuration properties for Spring Session.
 *
 * @author Gunnar Hillert
 * @since 3.0
 */
@ConfigurationProperties(prefix = "coherence.spring.session")
@Validated
public class CoherenceSpringSessionProperties {

	/**
	 * Name of the map used to store sessions.
	 */
	private String mapName = "spring:session:sessions";

	/**
	 * Sessions flush mode. Determines when session changes are written to the session
	 * store.
	 */
	private FlushMode flushMode = FlushMode.ON_SAVE;

	/**
	 * Sessions save mode. Determines how session changes are tracked and saved to the
	 * session store.
	 */
	private SaveMode saveMode = SaveMode.ON_SET_ATTRIBUTE;

	/**
	 * Shall a Coherence Entry Processor be used for handling updates to the persisted HTTP session? Defaults to true.
	 */
	private boolean useEntryProcessor = true;

	public String getMapName() {
		return this.mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public FlushMode getFlushMode() {
		return this.flushMode;
	}

	public void setFlushMode(FlushMode flushMode) {
		this.flushMode = flushMode;
	}

	public SaveMode getSaveMode() {
		return this.saveMode;
	}

	public void setSaveMode(SaveMode saveMode) {
		this.saveMode = saveMode;
	}

	public boolean getUseEntryProcessor() {
		return this.useEntryProcessor;
	}

	public void setUseEntryProcessor(boolean useEntryProcessor) {
		this.useEntryProcessor = useEntryProcessor;
	}
}
