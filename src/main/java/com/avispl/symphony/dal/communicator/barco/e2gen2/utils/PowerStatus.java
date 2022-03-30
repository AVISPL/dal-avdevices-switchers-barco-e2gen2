/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

/**
 * Power Status enum class
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public enum PowerStatus {

	NOT_PRESENT("Module not present", 0),
	CABLE_NOT_CONNECTED("Cable disconnected", 1),
	NO_DC_CURRENT("No DC current", 2),
	OK("OK", 3);

	private final String value;

	private final int code;

	/**
	 * PowerStatus with args constructor
	 *
	 * @param value value of PowerStatus
	 * @param code int code of PowerStatus
	 */
	PowerStatus(String value, int code) {
		this.value = value;
		this.code = code;
	}

	/**
	 * Retrieves {@code {@link #value}}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Retrieves {@code {@link #code}}
	 *
	 * @return value of {@link #code}
	 */
	public int getCode() {
		return code;
	}
}

