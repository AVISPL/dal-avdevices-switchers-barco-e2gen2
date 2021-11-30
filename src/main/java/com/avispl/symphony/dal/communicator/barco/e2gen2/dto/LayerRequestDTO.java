/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Layer request dto class
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public class LayerRequestDTO {

	@JsonProperty("id")
	private int id;

	/**
	 * LayerRequestDTO constructor
	 *
	 * @param id layer id
	 */
	public LayerRequestDTO(int id) {
		this.id = id;
	}

	/**
	 * Retrieves {@code {@link #id}}
	 *
	 * @return value of {@link #id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets {@code id}
	 *
	 * @param id the {@code int} field
	 */
	public void setId(int id) {
		this.id = id;
	}
}
