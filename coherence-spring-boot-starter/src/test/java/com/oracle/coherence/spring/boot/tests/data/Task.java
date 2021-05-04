/*
 * Copyright (c) 2013, 2021, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl.
 */
package com.oracle.coherence.spring.boot.tests.data;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import javax.persistence.Id;

/**
 * A data class representing a single To Do List task.
 *
 * @author Gunnar Hillert
 */
public class Task implements Serializable {

	/**
	 * The creation time.
	 */
	private long createdAt;

	/**
	 * The completion status.
	 */
	private Boolean completed;

	/**
	 * The task ID.
	 */
	@Id
	private String id;

	/**
	 * The task description.
	 */
	private String description;

	/**
	 * Deserialization constructor.
	 */
	public Task() {
	}

	/**
	 * Construct Task instance.
	 *
	 * @param description  task description
	 */
	public Task(String description) {
		this.id = UUID.randomUUID().toString().substring(0, 6);
		this.createdAt = System.currentTimeMillis();
		this.description = description;
		this.completed = false;
	}

	/**
	 * Get the creation time.
	 *
	 * @return the creation time
	 */
	public Long getCreatedAt() {
		return this.createdAt;
	}

	/**
	 * Get the task ID.
	 *
	 * @return the task ID
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Get the task description.
	 *
	 * @return the task description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Set the task description.
	 *
	 * @param description  the task description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Get the completion status.
	 *
	 * @return true if it is completed, false otherwise.
	 */
	public Boolean getCompleted() {
		return this.completed;
	}

	/**
	 * Sets the completion status.
	 *
	 * @param completed  the completion status
	 */
	public void setCompleted(Boolean completed) {
		this.completed = completed;
	}

	/**
	 * Returns the created date as a {@link LocalDateTime}.
	 *
	 * @return the created date as a {@link LocalDateTime}.
	 */
	public LocalDateTime getCreatedAtDate() {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(this.createdAt), ZoneId.systemDefault());
	}

	@Override
	public String toString() {
		return "Task{"
			+ "id=" + this.id
			+ ", description=" + this.description
			+ ", completed=" + this.completed
			+ '}';
		}
	}
