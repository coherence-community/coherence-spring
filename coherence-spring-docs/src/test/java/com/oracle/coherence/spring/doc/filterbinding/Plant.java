// tag::hide[]
/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.doc.filterbinding;

import java.io.Serializable;

// end::hide[]
public class Plant implements Serializable {
	private String name;
	private PlantType plantType;
	private int height;

	public Plant(String name, PlantType plantType, int height) {
		this.name = name;
		this.plantType = plantType;
		this.height = height;
	}

	public String getName() {
		return name;
	}

	public PlantType getPlantType() {
		return plantType;
	}

	public int getHeight() {
		return height;
	}
}
