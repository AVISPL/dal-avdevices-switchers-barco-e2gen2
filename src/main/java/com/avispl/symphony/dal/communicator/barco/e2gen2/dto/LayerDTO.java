/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Super/Screen Destination layer dto class
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public class LayerDTO {

	@JsonProperty("id")
	private int id;

	@JsonProperty("LastSrcIdx")
	private int lastSrcIdx;

	@JsonProperty("PvwMode")
	private int pvmMode;

	@JsonProperty("PgmMode")
	private int pgmMode;

	@JsonProperty("LinkLayerId")
	private int linkLayerId;

	@JsonProperty("LinkDestId")
	private int linkDestId;

	@JsonProperty("Window")
	private Dimension dimension;

	/**
	 * Layer DTO constructor
	 * @param id layer id
	 * @param lastSrcIdx source index of the layer
	 * @param pvmMode preview mode
	 * @param pgmMode program mode
	 */
	public LayerDTO(int id, int lastSrcIdx, int pvmMode, int pgmMode) {
		this.id = id;
		this.lastSrcIdx = lastSrcIdx;
		this.pvmMode = pvmMode;
		this.pgmMode = pgmMode;
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

	/**
	 * Retrieves {@code {@link #lastSrcIdx}}
	 *
	 * @return value of {@link #lastSrcIdx}
	 */
	public int getLastSrcIdx() {
		return lastSrcIdx;
	}

	/**
	 * Sets {@code lastSrcIdx}
	 *
	 * @param lastSrcIdx the {@code int} field
	 */
	public void setLastSrcIdx(int lastSrcIdx) {
		this.lastSrcIdx = lastSrcIdx;
	}

	/**
	 * Retrieves {@code {@link #pvmMode}}
	 *
	 * @return value of {@link #pvmMode}
	 */
	public int getPvmMode() {
		return pvmMode;
	}

	/**
	 * Sets {@code pvmMode}
	 *
	 * @param pvmMode the {@code int} field
	 */
	public void setPvmMode(int pvmMode) {
		this.pvmMode = pvmMode;
	}

	/**
	 * Retrieves {@code {@link #pgmMode}}
	 *
	 * @return value of {@link #pgmMode}
	 */
	public int getPgmMode() {
		return pgmMode;
	}

	/**
	 * Sets {@code pgmMode}
	 *
	 * @param pgmMode the {@code int} field
	 */
	public void setPgmMode(int pgmMode) {
		this.pgmMode = pgmMode;
	}

	/**
	 * Retrieves {@code {@link #dimension}}
	 *
	 * @return value of {@link #dimension}
	 */
	public Dimension getDimension() {
		return dimension;
	}

	/**
	 * Sets {@code dimension}
	 *
	 * @param dimension the {@code com.avispl.symphony.dal.communicator.barco.e2gen2.dto.Dimension} field
	 */
	public void setDimension(Dimension dimension) {
		this.dimension = dimension;
	}

	/**
	 * Retrieves {@code {@link #linkLayerId}}
	 *
	 * @return value of {@link #linkLayerId}
	 */
	public int getLinkLayerId() {
		return linkLayerId;
	}

	/**
	 * Sets {@code linkLayerId}
	 *
	 * @param linkLayerId the {@code int} field
	 */
	public void setLinkLayerId(int linkLayerId) {
		this.linkLayerId = linkLayerId;
	}

	/**
	 * Retrieves {@code {@link #linkDestId}}
	 *
	 * @return value of {@link #linkDestId}
	 */
	public int getLinkDestId() {
		return linkDestId;
	}

	/**
	 * Sets {@code linkDestId}
	 *
	 * @param linkDestId the {@code int} field
	 */
	public void setLinkDestId(int linkDestId) {
		this.linkDestId = linkDestId;
	}

}
