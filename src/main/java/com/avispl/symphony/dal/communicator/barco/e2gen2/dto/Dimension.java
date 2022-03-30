/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dimension DTO class
 *
 * @author Ivan
 * @since 1.0.0
 */
public class Dimension {

	@JsonProperty("HPos")
	private int hPos;

	@JsonProperty("VPos")
	private int vPos;

	@JsonProperty("HSize")
	private int hSize;

	@JsonProperty("VSize")
	private int vSize;

	/**
	 * Dimension no-arg constructor
	 */
	public Dimension() {
	}

	/**
	 * Dimension with args constructor
	 *
	 * @param hPos Horizontal position of layer
	 * @param vPos Vertical position of layer
	 * @param hSize Horizontal size of layer
	 * @param vSize Vertical size of layer
	 */
	public Dimension(int hPos, int vPos, int hSize, int vSize) {
		this.hPos = hPos;
		this.vPos = vPos;
		this.hSize = hSize;
		this.vSize = vSize;
	}

	/**
	 * Retrieves {@code {@link #hPos}}
	 *
	 * @return value of {@link #hPos}
	 */
	public int gethPos() {
		return hPos;
	}

	/**
	 * Sets {@code hPos}
	 *
	 * @param hPos the {@code int} field
	 */
	public void sethPos(int hPos) {
		this.hPos = hPos;
	}

	/**
	 * Retrieves {@code {@link #vPos}}
	 *
	 * @return value of {@link #vPos}
	 */
	public int getvPos() {
		return vPos;
	}

	/**
	 * Sets {@code vPos}
	 *
	 * @param vPos the {@code int} field
	 */
	public void setvPos(int vPos) {
		this.vPos = vPos;
	}

	/**
	 * Retrieves {@code {@link #hSize}}
	 *
	 * @return value of {@link #hSize}
	 */
	public int gethSize() {
		return hSize;
	}

	/**
	 * Sets {@code hSize}
	 *
	 * @param hSize the {@code int} field
	 */
	public void sethSize(int hSize) {
		this.hSize = hSize;
	}

	/**
	 * Retrieves {@code {@link #vSize}}
	 *
	 * @return value of {@link #vSize}
	 */
	public int getvSize() {
		return vSize;
	}

	/**
	 * Sets {@code vSize}
	 *
	 * @param vSize the {@code int} field
	 */
	public void setvSize(int vSize) {
		this.vSize = vSize;
	}
}
