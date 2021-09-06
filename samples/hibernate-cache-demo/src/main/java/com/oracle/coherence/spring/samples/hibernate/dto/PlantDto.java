/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.hibernate.dto;

/**
 * The Dto class a plant.
 * @author Gunnar Hillert
 */
public class PlantDto {

	private Long id;

	private String genus;
	private String species;
	private String commonName;

	public PlantDto() {
	}

	public PlantDto(String genus, String species, String commonName) {
		this.genus = genus;
		this.species = species;
		this.commonName = commonName;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGenus() {
		return this.genus;
	}

	public void setGenus(String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return this.species;
	}

	public void setSpecies(String species) {
		this.species = species;
	}

	public String getCommonName() {
		return this.commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
}
