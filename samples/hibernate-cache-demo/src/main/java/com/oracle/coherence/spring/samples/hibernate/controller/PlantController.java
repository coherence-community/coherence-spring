/*
 * Copyright (c) 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.samples.hibernate.controller;

import java.util.List;

import com.oracle.coherence.spring.samples.hibernate.dao.PlantRepository;
import com.oracle.coherence.spring.samples.hibernate.dto.PlantDto;
import com.oracle.coherence.spring.samples.hibernate.model.Plant;
import org.modelmapper.ModelMapper;

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

	private final PlantRepository plantRepository;

	private final ModelMapper modelMapper;


	public PlantController(PlantRepository plantRepository, ModelMapper modelMapper) {
		this.plantRepository = plantRepository;
		this.modelMapper = modelMapper;
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
	public Plant createPlant(@RequestBody PlantDto plantDto) {
		final Plant plantToSave = this.modelMapper.map(plantDto, Plant.class);
		return this.plantRepository.save(plantToSave);
	}

	@DeleteMapping("/{id}")
	public void deletePlant(@PathVariable Long id) {
		this.plantRepository.deleteById(id);
	}

}
