/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Power Status DTO
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public class PowerStatusDTO {
	@JsonProperty("PowerSupply1Status")
	private int powerSupply1Status;
	@JsonProperty("PowerSupply2Status")
	private int powerSupply2Status;

	/**
	 * Retrieves {@code {@link #powerSupply1Status}}
	 *
	 * @return value of {@link #powerSupply1Status}
	 */
	public int getPowerSupply1Status() {
		return powerSupply1Status;
	}

	/**
	 * Sets {@code powerSupply1Status}
	 *
	 * @param powerSupply1Status the {@code int} field
	 */
	public void setPowerSupply1Status(int powerSupply1Status) {
		this.powerSupply1Status = powerSupply1Status;
	}

	/**
	 * Retrieves {@code {@link #powerSupply2Status}}
	 *
	 * @return value of {@link #powerSupply2Status}
	 */
	public int getPowerSupply2Status() {
		return powerSupply2Status;
	}

	/**
	 * Sets {@code powerSupply2Status}
	 *
	 * @param powerSupply2Status the {@code int} field
	 */
	public void setPowerSupply2Status(int powerSupply2Status) {
		this.powerSupply2Status = powerSupply2Status;
	}
}
