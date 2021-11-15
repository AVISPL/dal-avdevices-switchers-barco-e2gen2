/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

/**
 * Barco E2 Constant class
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public final class BarcoE2Constant {
	public static final String SYSTEM = "System";
	public static final String RESULT = "result";
	public static final String RESPONSE = "response";
	public static final String DOUBLE_QUOTES = "";
	public static final String ID = "id";
	public static final String NAME = "Name";
	public static final String NULL = "null";
	public static final String TYPE = "type";
	public static final String LABEL_ACTIVATE_ON_PROGRAM = "Activate on Program";
	public static final String LABEL_PRESSED_RECALLING_PRESET = "Recalling preset ...";
	public static final String SUCCESS = "success";
	public static final String DEFAULT_RPC_VERSION = "2.0";
	public static final String MAC_ADDRESS = "MacAddress";
	public static final String FRAME_COLLECTION = "FrameCollection";
	public static final String FRAME = "Frame";
	public static final String VERSION = "Version";
	public static final String FRAME_TYPE_NAME = "FrameTypeName";
	public static final String ENET = "Enet";
	public static final String DHCP_MODE = "DhcpMode";
	public static final String IP = "IP";
	public static final String STATIC_IP = "StaticIP";
	public static final int DEFAULT_ID = 1234;
	// Preset
	public static final String PRESET_NAME = "presetName";
	public static final String NO_RECALLED_PRESET = "-1";
	public static final int LIST_ALL_DESTINATION_FOR_PRESET = -1;
	public static final String NO_PRESET_HAS_BEEN_RECALLED = "No preset has been recalled";
	// Methods
	public static final String METHOD_LIST_DESTINATIONS_FOR_PRESET = "listDestinationsForPreset";
	public static final String METHOD_LAST_RECALLED_PRESET = "lastRecalledPreset";
	public static final String METHOD_ACTIVATE_PRESET = "activatePreset";
	public static final String METHOD_POWER_STATUS = "powerStatus";
	public static final String METHOD_GET_FRAME_SETTINGS = "getFrameSettings";

	public static final int GRACE_PERIOD = 0;
}