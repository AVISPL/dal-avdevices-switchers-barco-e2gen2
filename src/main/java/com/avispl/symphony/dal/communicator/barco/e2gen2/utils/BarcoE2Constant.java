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
	public static final String PGM_LAST_SRC_INDEX = "PgmLastSrcIndex";
	public static final String PVM_LAST_SRC_INDEX = "PvwLastSrcIndex";
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
	public static final String NONE = "None";
	public static final String LAYERS = "Layers";
	public static final String STATIC_IP = "StaticIP";
	public static final String LAST_SRC_IDX = "LastSrcIdx";
	public static final int GRACE_PERIOD = 0;
	public static final int NOT_MATCH_AUX_ID = -1;
	public static final int NOT_MATCH_SCREEN_ID = -1;
	public static final String HASH_TAG = "#";
	public static final int DEFAULT_ID = 1234;
	public static final int ACTIVE_PRESET_ON_PROGRAM = 1;
	public static final int SHOW_ALL_DESTINATION = 0;
	public static final String SUCCESS_STATUS = "successCode";
	public static final String LINK_LAYER_ID = "LinkLayerId";
	public static final String LINK_DEST_ID = "LinkDestId";
	public static final String PVW_MODE = "PvwMode";

	// Preset
	public static final String PRESET_NAME = "presetName";
	public static final int NO_RECALLED_PRESET = -1;
	public static final int LIST_ALL_DESTINATION_FOR_PRESET = -1;

	// Methods
	public static final String METHOD_LIST_DESTINATIONS_FOR_PRESET = "listDestinationsForPreset";
	public static final String METHOD_LAST_RECALLED_PRESET = "lastRecalledPreset";
	public static final String METHOD_ACTIVATE_PRESET = "activatePreset";
	public static final String METHOD_POWER_STATUS = "powerStatus";
	public static final String METHOD_GET_FRAME_SETTINGS = "getFrameSettings";
	public static final String METHOD_LIST_SOURCES = "listSources";
	public static final String METHOD_LIST_DESTINATIONS = "listDestinations";
	public static final String METHOD_LIST_CONTENT = "listContent";
	public static final String METHOD_LIST_AUX_CONTENT = "listAuxContent";
	public static final String METHOD_CHANGE_AUX_CONTENT = "changeAuxContent";
	public static final String METHOD_CHANGE_CONTENT = "changeContent";
	public static final String METHOD_LIST_SUPER_DEST_CONTENT = "listSuperDestContent";
	public static final String METHOD_LIST_SUPER_AUX_CONTENT = "listSuperAuxContent";
	public static final String METHOD_CLEAR_LAYERS = "clearLayers";

	// Destinations
	public static final String DESTINATION_STATUS = "Status";
	public static final String DESTINATION_MIXED = "Mixed";
	public static final int DESTINATION_SCREEN_TYPE = 1;
	public static final int DESTINATION_AUX_TYPE = 2;
	public static final String DASH = "-";
	public static final String COMMA = ",";
	public static final String DEFAULT_JSONRPC = "jsonrpc";
	public static final int DEFAULT_RPC_ID = 1234;
	public static final String H_DIMENTION = "HDimention";
	public static final String V_DIMENTION = "VDimention";
	public static final String H_SIZE = "HSize";
	public static final String V_SIZE = "VSize";
	public static final String AUX_DEST_COLLECTION = "AuxDestCollection";
	public static final String DEST_COLLECTION = "DestCollection";
	public static final String GROUP_HASH_TAG_MEMBER = "%s#%s";
	public static final String GROUP_DEST_HASH_TAG_DEST_NAME_COLON_MEMBER = "%s#%s-%s";
	public static final String SCREEN_ID = "screenId";

	public static final String GLOBAL_LAYER_COLLECTION = "GlobalLayerCollection";
	public static final String GLOBAL_LAYER = "GlobalLayer";
	public static final String NOT_FOUND_LAYER = "NotFoundLayer";
	public static final String GLOBAL_LAYERS = "GlobalLayers";
	public static final String PGM_MODE = "PgmMode";
}

