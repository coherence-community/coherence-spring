/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.hibernate.controller;

import java.util.List;

import com.oracle.coherence.spring.samples.hibernate.dao.PlantRepository;
import com.oracle.coherence.spring.samples.hibernate.model.Plant;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main REST Controller.
 * @author Gunnar Hillert
 */
@RestController
@RequestMapping("/plants")
public class PlantController {

	private PlantRepository plantRepository;

	public PlantController(PlantRepository plantRepository) {
		this.plantRepository = plantRepository;
	}

	@GetMapping
	public List<Plant> getPlants(@RequestParam(name = "use-cache", defaultValue = "false") boolean useCache) {
		if (useCache) {
			return this.plantRepository.getAllPlantsCacheable();
		}
		else {
			return this.plantRepository.findAll();
		}
	}

	@GetMapping("/{id}")
	public Plant getSinglePlant(@PathVariable Long id) {
		return this.plantRepository.findById(id).get();
	}

	@PostMapping
	public Plant createPlant(@RequestBody Plant plant) {
		return this.plantRepository.save(plant);
	}

	@DeleteMapping("/{id}")
	public void deletePlant(@PathVariable Long id) {
		this.plantRepository.deleteById(id);
	}

}
