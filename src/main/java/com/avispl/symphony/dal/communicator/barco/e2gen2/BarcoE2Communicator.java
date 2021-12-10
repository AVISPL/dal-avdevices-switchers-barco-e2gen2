/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.DeviceInfo;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.Dimension;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.LayerDTO;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.LayerRequestDTO;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.PowerStatusDTO;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.RpcRequest;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.RpcResponse;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.AuxDestination;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.SuperAuxDestination;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.SuperDestination;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2Constant;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2ControllingMetric;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2MonitoringMetric;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.PowerStatus;

/**
 * An implementation of RestCommunicator to provide communication and interaction with Barco E2 Gen2 devices.
 * Supported features are:
 * Monitoring:
 * <ul>
 * <li> - Online / Offline Status</li>
 * <li> - Firmware Version</li>
 * <li> - IP Address</li>
 * <li> - Device ID</li>
 * <li> - Device Model</li>
 * <li> - Device Name</li>
 * <li> - Hostname</li>
 * <li> - MAC Address</li>
 * <li> - Routing Status</li>
 * <li> - Preset Feedback</li>
 * <li> - Connected Units</li>
 * <li> - Power Status</li>
 * </ul>
 * Controlling:
 * - Routing Control
 * - Preset Control
 *
 * @author Duy Nguyen, Ivan
 * @since 1.0.0
 */
public class BarcoE2Communicator extends RestCommunicator implements Monitorable, Controller {

	/**
	 * Routing control: class to store properties of source
	 */
	class SourceProperties {
		String currentSourceName = BarcoE2Constant.DOUBLE_QUOTES;
		int numberOfSource = 0;
	}

	private String lastPresetName = BarcoE2Constant.DOUBLE_QUOTES;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private List<Integer> listSuperDestId = new ArrayList<>();
	private List<Integer> listSuperAuxId = new ArrayList<>();
	private Map<Integer, String> sourceIdToNameMap = new HashMap<>();
	private boolean isFailRetrieveMetaData = false;

	private String listSuperScreenDestId;
	private String listSuperAuxDestId;

	/**
	 * Retrieves {@code {@link #listSuperScreenDestId}}
	 *
	 * @return value of {@link #listSuperScreenDestId}
	 */
	public String getListSuperScreenDestId() {
		return listSuperScreenDestId;
	}

	/**
	 * Sets {@code listSuperScreenDestId}
	 *
	 * @param listSuperScreenDestId the {@code java.lang.String} field
	 */
	public void setListSuperScreenDestId(String listSuperScreenDestId) {
		this.listSuperScreenDestId = listSuperScreenDestId;
	}

	/**
	 * Retrieves {@code {@link #listSuperAuxDestId}}
	 *
	 * @return value of {@link #listSuperAuxDestId}
	 */
	public String getListSuperAuxDestId() {
		return listSuperAuxDestId;
	}

	/**
	 * Sets {@code listSuperAuxDestId}
	 *
	 * @param listSuperAuxDestId the {@code java.lang.String} field
	 */
	public void setListSuperAuxDestId(String listSuperAuxDestId) {
		this.listSuperAuxDestId = listSuperAuxDestId;
	}

	/**
	 * {@inheritDoc}
	 * BarcoE2Communicator doesn't require authentication
	 */
	@Override
	protected void authenticate() {
		// BarcoE2Communicator doesn't require authentication
	}

	/**
	 * {@inheritDoc}
	 * Properties that need to be controlled:
	 * - Routing Control
	 * - Preset Control
	 *
	 * @param controllableProperty control property that will be controlled
	 * @throws Exception when fail to control property
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		String property = controllableProperty.getProperty();
		String value = String.valueOf(controllableProperty.getValue());
		String propertyMethod = property.substring(0, property.indexOf(BarcoE2Constant.HASH_TAG));
		String propertyValue = property.substring(property.indexOf(BarcoE2Constant.HASH_TAG) + 1);
		if (propertyMethod.contains(BarcoE2ControllingMetric.SUPER_SCREEN_DESTINATION.getName() + BarcoE2Constant.COLON) || propertyMethod.contains(
				BarcoE2ControllingMetric.SUPER_AUX_DESTINATION.getName() + BarcoE2Constant.COLON)) {
			String[] splitDestString = propertyMethod.split(BarcoE2Constant.COLON);
			String firstItem = splitDestString[0].trim();
			if (firstItem.equals(BarcoE2ControllingMetric.SUPER_SCREEN_DESTINATION.getName()) || firstItem.equals(BarcoE2ControllingMetric.SUPER_AUX_DESTINATION.getName())) {
				propertyMethod = splitDestString[0];
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Controlling device with property: %s and value: %s", property, value));
		}
		BarcoE2ControllingMetric barcoE2ControllingMetric = getBarcoE2ControllingMetric(property, propertyMethod);
		switch (barcoE2ControllingMetric) {
			case PRESETS_PRESET:
				lastPresetName = value;
				break;
			case PRESETS_PRESET_ACTIVATE:
				controlActivatePreset();
				break;
			case SCREEN_DESTINATIONS:
				controlAssignSourceToScreenDest(value, propertyValue);
				break;
			case SUPER_SCREEN_DESTINATION:
				controlAssignSourceToSuperDest(value, propertyValue);
				break;
			case AUX_DESTINATIONS:
				controlAssignSourceToAuxDest(value, propertyValue);
				break;
			case SUPER_AUX_DESTINATION:
				controlAssignSourceToSuperAuxDest(value, propertyValue);
				break;
			default:
				if (logger.isWarnEnabled()) {
					logger.warn(String.format("Operation %s with value %s is not supported.", property, value));
				}
				throw new IllegalArgumentException(String.format("Operation %s with value %s is not supported.", property, value));
		}
	}

	/**
	 * {@inheritDoc}
	 * Control properties
	 *
	 * @param list list of control properties that will be controlled
	 * @throws Exception when fail to control properties
	 */
	@Override
	public void controlProperties(List<ControllableProperty> list) throws Exception {
		if (CollectionUtils.isEmpty(list)) {
			throw new IllegalArgumentException("Controllable properties cannot be null or empty");
		}
		for (ControllableProperty controllableProperty : list) {
			controlProperty(controllableProperty);
		}
	}

	/**
	 * {@inheritDoc}
	 * This method is called by Symphony to get the list of statistics to be displayed
	 *
	 * @return List<Statistics> This returns the list of statistics
	 * @throws Exception Throw exception when fail to get info from device
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Getting statistics from the device at host %s with port %s", this.host, this.getPort()));
		}
		Map<String, String> statistics = new HashMap<>();
		List<AdvancedControllableProperty> controls = new ArrayList<>();
		ExtendedStatistics extendedStatistics = new ExtendedStatistics();
		initializeData(statistics, controls);
		extendedStatistics.setStatistics(statistics);
		extendedStatistics.setControllableProperties(controls);
		return Collections.singletonList(extendedStatistics);

	}

	/**
	 * Prepare meta data for the device
	 */
	private void prepareDeviceMetaData() {
		if (listSuperScreenDestId != null) {
			listSuperDestId = handleListSuperId(true);
		} else {
			listSuperDestId = Collections.emptyList();
		}
		if (listSuperAuxDestId != null) {
			listSuperAuxId = handleListSuperId(false);
		} else {
			listSuperAuxId = Collections.emptyList();
		}
		try {
			sourceIdToNameMap = getSourceIdToSourceNameMap();
			isFailRetrieveMetaData = false;
		} catch (Exception e) {
			logger.error("Fail to monitor and control routing part");
			isFailRetrieveMetaData = true;
		}
	}

	/**
	 * Call post request on the device to get RpcResponse DTO
	 *
	 * @param method String name of the method
	 * @param param Map of params
	 * @return JsonNode returns the JsonNode of given object
	 * @throws Exception Throw exception when fail to call post request, get data from device
	 */
	protected JsonNode requestByMethod(String method, Map<Object, Object> param) throws Exception {
		RpcResponse rpcResponse;
		rpcResponse = this.doPost(BarcoE2Constant.DOUBLE_QUOTES, rpcRequestBody(method, param), RpcResponse.class);
		if (rpcResponse == null) {
			throw new ResourceNotReachableException("doPost success but fail to get data from the device");
		}
		JsonNode response = rpcResponse.getResponse();
		if (response != null && !NullNode.instance.equals(response) && !response.isEmpty()) {
			return response;
		}
		// RETURN THE JsonNode of RpcResponse
		return objectMapper.valueToTree(rpcResponse);
	}

	/**
	 * Initialize monitoring/controlling data
	 *
	 * @param statistics Map of statistics
	 * @param controls List of AdvancedControllableProperty that need to be controlled
	 * @throws Exception Throw exception when fail to get preset/routing monitoring and controlling properties
	 */
	private void initializeData(Map<String, String> statistics, List<AdvancedControllableProperty> controls) throws Exception {
		getDeviceInformation(statistics);
		getPresetFeedBack(statistics, controls);
		prepareDeviceMetaData();
		if (!isFailRetrieveMetaData) {
			getRoutingControl(true, statistics, controls);
			getRoutingControl(false, statistics, controls);
			getSuperRoutingControl(true, statistics, controls);
			getSuperRoutingControl(false, statistics, controls);
		}
	}

	/**
	 * Generate RpcRequest DTO
	 *
	 * @param method Rpc method
	 * @param params Map of parameters
	 * @return This returns RpcRequest DTO
	 */
	private RpcRequest rpcRequestBody(String method, Map<Object, Object> params) {
		RpcRequest rpcRequest = new RpcRequest();
		rpcRequest.setMethod(method);
		rpcRequest.setParams(params);
		return rpcRequest;
	}

	/**
	 * Map a JsonNode to DTO
	 *
	 * @param jsonNode input jsonNode that need to be converted
	 * @param tClass Class that will be converted to
	 * @return This returns the DTO of given Object.
	 */
	private <T> Object jsonNodeToDTO(JsonNode jsonNode, Class<T> tClass) throws JsonProcessingException {
		return objectMapper.treeToValue(jsonNode, tClass);
	}

	/**
	 * controlProperty: Assign source to Super Aux Destination
	 *
	 * @param destName Destination name that will be assigned a new source to
	 * @param sourceName name of the new source
	 * @throws Exception when fail to change aux content
	 */
	private void controlAssignSourceToSuperAuxDest(String sourceName, String destName) throws Exception {
		try {
			changeAuxContent(destName, sourceName);
		} catch (Exception e) {
			if (e instanceof ResourceNotReachableException || e instanceof CommandFailureException) {
				throw e;
			}
			throw new CommandFailureException(this.getAddress(), "super aux destination routing control", "Fail to assign source to super aux destination", e);
		}
	}

	/**
	 * controlProperty: Assign source to Aux Destination
	 *
	 * @param destName Destination name that will be assigned a new source to
	 * @param sourceName name of the new source
	 * @throws Exception when fail to change aux content
	 */
	private void controlAssignSourceToAuxDest(String sourceName, String destName) throws Exception {
		try {
			changeAuxContent(destName, sourceName);
		} catch (Exception e) {
			if (e instanceof ResourceNotReachableException || e instanceof CommandFailureException) {
				throw e;
			}
			throw new CommandFailureException(this.getAddress(), "aux destination routing control", "Fail to assign source to aux destination", e);
		}
	}

	/**
	 * controlProperty: Assign source to Screen Destination
	 *
	 * @param destName Destination name that will be assigned a new source to
	 * @param sourceName name of the new source
	 * @throws Exception when fail to assign source
	 */
	private void controlAssignSourceToScreenDest(String sourceName, String destName) throws Exception {
		try {
			changeScreenContent(destName, sourceName);
		} catch (Exception e) {
			if (e instanceof ResourceNotReachableException || e instanceof CommandFailureException) {
				throw e;
			}
			throw new CommandFailureException(this.getAddress(), "screen destination routing control", "Fail to assign source to screen destination", e);
		}
	}

	/**
	 * controlProperty: Assign source to Super Destination
	 *
	 * @param destName Destination name that will be assigned a new source to
	 * @param sourceName name of the new source
	 * @throws Exception when fail to assign source
	 */
	private void controlAssignSourceToSuperDest(String sourceName, String destName) throws Exception {
		try {
			changeScreenContent(destName, sourceName);
		} catch (Exception e) {
			if (e instanceof ResourceNotReachableException || e instanceof CommandFailureException) {
				throw e;
			}
			throw new CommandFailureException(this.getAddress(), "super screen destination routing control", "Fail to assign source to super screen destination", e);
		}
	}

	/**
	 * controlProperty: Activate a preset.
	 */
	private void controlActivatePreset() {
		try {
			boolean result;
			result = activatePresetResult();
			if (logger.isDebugEnabled()) {
				String debugString = result ? String.format("Activate %s success!", lastPresetName) : String.format("Activate %s fail!", lastPresetName);
				logger.debug(debugString);
			}
		} catch (Exception e) {
			if (e instanceof ResourceNotReachableException || e instanceof CommandFailureException) {
				throw e;
			}
			throw new CommandFailureException(this.getAddress(), "preset control", "Fail to activate preset", e);
		}
	}

	/**
	 * Get Controlling metric based on preset/ routing metric
	 *
	 * @param property String of the property from controlProperty
	 * @param propertyMethod String that is split by property
	 * @return Return instance of BarcoE2ControllingMetric that based on preset/ routing metric
	 */
	private BarcoE2ControllingMetric getBarcoE2ControllingMetric(String property, String propertyMethod) {
		BarcoE2ControllingMetric barcoE2ControllingMetric;
		if (propertyMethod.equals(BarcoE2ControllingMetric.SCREEN_DESTINATIONS.getName()) || propertyMethod.equals(BarcoE2ControllingMetric.AUX_DESTINATIONS.getName())
				|| propertyMethod.equals(BarcoE2ControllingMetric.SUPER_SCREEN_DESTINATION.getName()) || propertyMethod.equals(BarcoE2ControllingMetric.SUPER_AUX_DESTINATION.getName())) {
			barcoE2ControllingMetric = BarcoE2ControllingMetric.getByName(propertyMethod);
		} else {
			barcoE2ControllingMetric = BarcoE2ControllingMetric.getByName(property);
		}
		return barcoE2ControllingMetric;
	}

	/**
	 * Device Information: Retrieve device information.
	 *
	 * @param stats Map of statistics
	 */
	private void getDeviceInformation(Map<String, String> stats) {
		try {
			JsonNode deviceInfoResponse = requestByMethod(BarcoE2Constant.METHOD_GET_FRAME_SETTINGS, new HashMap<>()).get(BarcoE2Constant.SYSTEM);
			// put monitoring data to stats
			if (deviceInfoResponse == null) {
				populateNoneForNullFieldInDeviceInfo(stats);
			} else {
				DeviceInfo deviceInfo = (DeviceInfo) jsonNodeToDTO(deviceInfoResponse, DeviceInfo.class);
				String macAddress = deviceInfo.getMacAddress();
				JsonNode powerStatusResponse = requestByMethod(BarcoE2Constant.METHOD_POWER_STATUS, new HashMap<>());
				deviceInfo.setConnectedUnit(powerStatusResponse.size());
				PowerStatusDTO powerStatusDTO = (PowerStatusDTO) jsonNodeToDTO(powerStatusResponse.get(macAddress), PowerStatusDTO.class);
				populateDeviceInformationData(stats, deviceInfo, powerStatusDTO);
			}
		} catch (Exception e) {
			populateNoneForNullFieldInDeviceInfo(stats);
			logger.error("Fail to get device information");
		}
	}

	/**
	 * Device Information: Populate data for device information
	 *
	 * @param stats Map of statistic
	 * @param deviceInfo DeviceInfo DTO
	 * @param powerStatusDTO PowerStatusDTO DTO
	 */
	private void populateDeviceInformationData(Map<String, String> stats, DeviceInfo deviceInfo, PowerStatusDTO powerStatusDTO) {
		stats.put(BarcoE2MonitoringMetric.POWER_SUPPLY_1_STATUS.getName(), getPowerStatus(powerStatusDTO.getPowerSupply1Status()));
		stats.put(BarcoE2MonitoringMetric.POWER_SUPPLY_2_STATUS.getName(), getPowerStatus(powerStatusDTO.getPowerSupply2Status()));
		stats.put(BarcoE2MonitoringMetric.CONNECTED_UNITS.getName(), String.valueOf(deviceInfo.getConnectedUnit()));
		stats.put(BarcoE2MonitoringMetric.FIRMWARE_VERSION.getName(), deviceInfo.getFrameWareVersion());
		stats.put(BarcoE2MonitoringMetric.IP_ADDRESS.getName(), deviceInfo.isDhcpMode() ? deviceInfo.getIp() : deviceInfo.getStaticIP());
		stats.put(BarcoE2MonitoringMetric.DEVICE_NAME.getName(), deviceInfo.getName());
		stats.put(BarcoE2MonitoringMetric.DEVICE_ID.getName(), deviceInfo.getDeviceId());
		stats.put(BarcoE2MonitoringMetric.DEVICE_MODEL.getName(), deviceInfo.getFrameTypeName());
		stats.put(BarcoE2MonitoringMetric.MAC_ADDRESS.getName(), deviceInfo.getMacAddress());
		stats.put(BarcoE2MonitoringMetric.HOST_NAME.getName(), deviceInfo.getName());
	}

	/**
	 * Device Information: Populate None data for device information
	 *
	 * @param stats Map of statistic
	 */
	private void populateNoneForNullFieldInDeviceInfo(Map<String, String> stats) {
		stats.put(BarcoE2MonitoringMetric.POWER_SUPPLY_1_STATUS.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.POWER_SUPPLY_2_STATUS.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.CONNECTED_UNITS.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.FIRMWARE_VERSION.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.IP_ADDRESS.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.DEVICE_NAME.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.DEVICE_ID.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.DEVICE_MODEL.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.MAC_ADDRESS.getName(), BarcoE2Constant.NONE);
		stats.put(BarcoE2MonitoringMetric.HOST_NAME.getName(), BarcoE2Constant.NONE);
	}

	/**
	 * Device Information: Convert int power status to String
	 *
	 * @param powerSupplyStatus int value of the powerStatus
	 * @return String This returns the String of power status.
	 */
	private String getPowerStatus(int powerSupplyStatus) {
		String powerStatus = BarcoE2Constant.DOUBLE_QUOTES;
		if (powerSupplyStatus == PowerStatus.NOT_PRESENT.getCode()) {
			powerStatus = PowerStatus.NOT_PRESENT.getValue();
		} else if (powerSupplyStatus == PowerStatus.CABLE_NOT_CONNECTED.getCode()) {
			powerStatus = PowerStatus.CABLE_NOT_CONNECTED.getValue();
		} else if (powerSupplyStatus == PowerStatus.NO_DC_CURRENT.getCode()) {
			powerStatus = PowerStatus.NO_DC_CURRENT.getValue();
		} else if (powerSupplyStatus == PowerStatus.OK.getCode()) {
			powerStatus = PowerStatus.OK.getValue();
		}
		return powerStatus;
	}

	/**
	 * Preset Control: Monitor & control
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @throws Exception Throw exceptions if fail to get JsonNode
	 */
	private void getPresetFeedBack(Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		// Generate dropdown options
		List<String> dropDownOptions = generateDropDownOptions();
		// Set lastPresetName to first index of dropdown, else lastPresetName = activePresetResult
		String activePresetResult = getActivePresetName();
		if (BarcoE2Constant.NONE.equals(activePresetResult) && !dropDownOptions.isEmpty()) {
			lastPresetName = dropDownOptions.get(0);
		}
		// Populate data to to monitor/control preset.
		populatePresetStatsAndControls(stats, controls, dropDownOptions, activePresetResult);
	}

	/**
	 * Preset Control: Add presets to dropdown list
	 *
	 * @return This returns List<String> of presets
	 * @throws Exception Throw exception if fail to get JsonNode
	 */
	private List<String> generateDropDownOptions() throws Exception {
		Map<Object, Object> presetParam = new HashMap<>();
		presetParam.put(BarcoE2Constant.ID, BarcoE2Constant.LIST_ALL_DESTINATION_FOR_PRESET);
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS_FOR_PRESET, presetParam);
		List<String> dropDownOptions = new ArrayList<>();
		JsonNode responseNode = response.get(BarcoE2Constant.RESPONSE);
		if (responseNode != null && responseNode.isEmpty()) {
			return Collections.emptyList();
		}
		for (int i = 0; i < response.size(); i++) {
			JsonNode responseNodeName = response.get(i);
			JsonNode destinationName = responseNodeName.get(BarcoE2Constant.NAME);
			if (destinationName != null) {
				dropDownOptions.add(destinationName.asText());
			}
		}
		return dropDownOptions;
	}

	/**
	 * Preset Control: Get current activate preset name
	 *
	 * @return This returns a preset name
	 * @throws Exception Throw exception if fail to get json node
	 */
	private String getActivePresetName() throws Exception {
		JsonNode recallPresetResponse = requestByMethod(BarcoE2Constant.METHOD_LAST_RECALLED_PRESET, new HashMap<>());
		JsonNode response;
		if (recallPresetResponse.isNumber()) {
			response = recallPresetResponse;
		} else {
			JsonNode recallPresetResponseNode = recallPresetResponse.get(BarcoE2Constant.RESPONSE);
			if (recallPresetResponseNode != null) {
				response = recallPresetResponseNode;
			} else {
				return BarcoE2Constant.NONE;
			}
		}
		String activePresetResult;
		int activePresetIndex = response.asInt();
		Map<Object, Object> presetParam = new HashMap<>();
		presetParam.put(BarcoE2Constant.ID, activePresetIndex);
		JsonNode presetResponse = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS_FOR_PRESET, presetParam);
		if (presetResponse == null) {
			return BarcoE2Constant.NONE;
		}
		JsonNode presetResponseStatus = presetResponse.get(BarcoE2Constant.SUCCESS);
		if (presetResponseStatus != null && presetResponseStatus.asInt() != 0) {
			// error message from the device
			JsonNode presetRes = response.get(BarcoE2Constant.RESPONSE);
			if (logger.isDebugEnabled() && presetRes != null) {
				logger.debug(presetRes);
			}
			return BarcoE2Constant.NONE;
		}
		JsonNode presetRes = presetResponse.get(BarcoE2Constant.RESPONSE);
		if (presetRes != null && presetRes.isEmpty() && presetResponse.get(BarcoE2Constant.NAME) == null) {
			return BarcoE2Constant.NONE;
		}
		activePresetResult = presetResponse.get(BarcoE2Constant.NAME).asText();
		return activePresetResult;
	}

	/**
	 * Preset Control: Populate data for preset
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @param dropDownOptions List of dropdown options
	 * @param activePresetResult Activate preset name
	 */
	private void populatePresetStatsAndControls(Map<String, String> stats, List<AdvancedControllableProperty> controls, List<String> dropDownOptions, String activePresetResult) {
		if (dropDownOptions.isEmpty()) {
			stats.put(BarcoE2ControllingMetric.PRESETS_PRESET.getName(), BarcoE2Constant.NONE);
			stats.put(BarcoE2ControllingMetric.PRESETS_LAST_CALLED_PRESET.getName(), BarcoE2Constant.NONE);
			return;
		} else {
			stats.put(BarcoE2ControllingMetric.PRESETS_PRESET.getName(), activePresetResult);
			controls.add(createDropdown(BarcoE2ControllingMetric.PRESETS_PRESET.getName(), activePresetResult, dropDownOptions));
		}
		stats.put(BarcoE2ControllingMetric.PRESETS_LAST_CALLED_PRESET.getName(), activePresetResult);
		stats.put(BarcoE2ControllingMetric.PRESETS_PRESET_ACTIVATE.getName(), activePresetResult);
		controls.add(createButton(BarcoE2ControllingMetric.PRESETS_PRESET_ACTIVATE.getName(),
				BarcoE2Constant.LABEL_ACTIVATE_ON_PROGRAM, BarcoE2Constant.LABEL_PRESSED_RECALLING_PRESET, BarcoE2Constant.GRACE_PERIOD));
	}

	/**
	 * Preset Control: Activate the preset
	 *
	 * @return Boolean this returns true/false based on the result of activating preset
	 */
	private boolean activatePresetResult() {
		try {
			boolean result;
			if (Objects.equals(lastPresetName, BarcoE2Constant.DOUBLE_QUOTES)) {
				// first time running
				String activePresetResult = getActivePresetName();
				if (activePresetResult.equals(BarcoE2Constant.NONE)) {
					result = activatePreset(lastPresetName);
				} else {
					result = activatePreset(activePresetResult);
				}
			} else {
				result = activatePreset(lastPresetName);
			}
			return result;
		} catch (Exception e) {
			throw new CommandFailureException(this.getAddress(), "activatePreset", "Fail to activate preset", e);
		}
	}

	/**
	 * Preset Control: Activate a preset by name and type
	 *
	 * @param presetName name of the preset that need to be activated
	 * @return boolean type indicates that preset is successfully activated.
	 * @throws Exception Throw exception when fail to get JsonNode
	 */
	private boolean activatePreset(String presetName) throws Exception {
		// Check if preset is exist
		Map<Object, Object> presetParam = new HashMap<>();
		presetParam.put(BarcoE2Constant.ID, BarcoE2Constant.LIST_ALL_DESTINATION_FOR_PRESET);
		JsonNode allPreset = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS_FOR_PRESET, presetParam);
		if (allPreset == null) {
			throw new ResourceNotReachableException("Fail to get list of preset");
		}
		boolean presetStatus = false;
		for (int i = 0; i < allPreset.size(); i++) {
			if (allPreset.get(i) != null) {
				JsonNode presetNameNode = allPreset.get(i).get(BarcoE2Constant.NAME);
				if (presetNameNode != null && presetNameNode.asText().equals(presetName)) {
					presetStatus = true;
					break;
				}
			}
		}
		if (!presetStatus) {
			throw new ResourceNotReachableException(String.format("Preset with name: %s not exists", presetName));
		}
		// active preset
		Map<Object, Object> param = new HashMap<>();
		param.put(BarcoE2Constant.PRESET_NAME, presetName);
		param.put(BarcoE2Constant.TYPE, BarcoE2Constant.ACTIVE_PRESET_ON_PROGRAM);
		JsonNode activatePresetResponse = requestByMethod(BarcoE2Constant.METHOD_ACTIVATE_PRESET, param);
		JsonNode activatePresetSuccessNode = activatePresetResponse.get(BarcoE2Constant.SUCCESS_STATUS);
		if (activatePresetSuccessNode == null) {
			throw new CommandFailureException(this.getAddress(), "activatePreset", "Fail to activatePreset");
		}
		return activatePresetSuccessNode.asInt() == 0;
	}

	/**
	 * Routing control: Update the source properties value
	 *
	 * @param isScreenDest boolean true/false based on screen/aux destination
	 * @param destContent JsonNode content of the destination
	 * @param sourceProperties Static class for properties of source
	 */
	public void updateSourcePropertiesValue(boolean isScreenDest, JsonNode destContent, SourceProperties sourceProperties) {
		if (isScreenDest) {
			updateSourcePropertiesForScreenDest(destContent, sourceProperties);
		} else {
			updateSourcePropertiesForAuxDest(destContent, sourceProperties);
		}
	}

	/**
	 * Update source properties for aux destination
	 *
	 * @param destContent content of aux destination
	 * @param sourceProperties SourceProperties class
	 */
	private void updateSourcePropertiesForAuxDest(JsonNode destContent, SourceProperties sourceProperties) {
		if (destContent.get(BarcoE2Constant.PGM_LAST_SRC_INDEX) == null) {
			return;
		}
		int lastSrcIndex = destContent.get(BarcoE2Constant.PGM_LAST_SRC_INDEX).asInt();
		// Assign number of source to 1 because Aux Destination only have 1 source.
		if (lastSrcIndex == -1) {
			sourceProperties.currentSourceName = BarcoE2Constant.NONE;
		} else {
			sourceProperties.numberOfSource = 1;
			sourceProperties.currentSourceName = sourceIdToNameMap.get(lastSrcIndex);
		}
	}

	/**
	 * Update source properties for screen destination
	 *
	 * @param destContent content of screen destination
	 * @param sourceProperties SourceProperties class
	 */
	private void updateSourcePropertiesForScreenDest(JsonNode destContent, SourceProperties sourceProperties) {
		JsonNode layers = destContent.get(BarcoE2Constant.LAYERS);
		List<Integer> listSourceIndexes = new ArrayList<>();
		for (int j = 0; j < layers.size(); j++) {
			JsonNode currentLayerNode = layers.get(j);
			if (currentLayerNode == null) {
				continue;
			}
			int sourceIndex = currentLayerNode.get(BarcoE2Constant.LAST_SRC_IDX).asInt();
			int pgmMode = currentLayerNode.get(BarcoE2Constant.PGM_MODE).asInt();
			int linkDestId = currentLayerNode.get(BarcoE2Constant.LINK_DEST_ID).asInt();
			if (sourceIndex != -1 && pgmMode == 1 && sourceIdToNameMap.get(sourceIndex) != null && linkDestId == -1) {
				listSourceIndexes.add(sourceIndex);
			}
		}
		sourceProperties.numberOfSource = listSourceIndexes.size();
		if (!listSourceIndexes.isEmpty()) {
			sourceProperties.currentSourceName = sourceIdToNameMap.get(listSourceIndexes.get(0));
		} else {
			sourceProperties.currentSourceName = BarcoE2Constant.NONE;
		}
	}

	/**
	 * Routing control: Screen + Aux Destination
	 *
	 * @param isScreenDest True: "ScreenDestination", false: "AuxDestination"
	 * @param controls List of AdvancedControllableProperty
	 * @param stats Map of statistics
	 * @throws Exception Throw exception if fail to get json node
	 */
	private void getRoutingControl(boolean isScreenDest, Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String methodName = isScreenDest ? BarcoE2Constant.SCREEN_DESTINATION : BarcoE2Constant.AUX_DESTINATION;
		JsonNode response = getRoutingControlJsonNode(methodName);
		if (response == null) {
			return;
		}
		// Screen/Aux dest loop
		for (int i = 0; i < response.size(); i++) {
			JsonNode currentDestContent = response.get(i);
			if (currentDestContent == null || currentDestContent.get(BarcoE2Constant.ID) == null || currentDestContent.get(BarcoE2Constant.NAME) == null) {
				continue;
			}
			String screenDestName = currentDestContent.get(BarcoE2Constant.NAME).asText();
			JsonNode destContent = getDestContent(isScreenDest, response, i);
			String groupType = isScreenDest ? BarcoE2ControllingMetric.SCREEN_DESTINATIONS.getName() : BarcoE2ControllingMetric.AUX_DESTINATIONS.getName();
			if (isScreenDest) {
				JsonNode layerNode = destContent.get(BarcoE2Constant.LAYERS);
				if (layerNode == null) {
					continue;
				}
				int firstNormalLayerIndex = -1;
				firstNormalLayerIndex = getFirstNormalLayerIndex(layerNode, firstNormalLayerIndex);
				if (firstNormalLayerIndex == -1) {
					stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, groupType, screenDestName), BarcoE2Constant.NOT_FOUND_LAYER);
					continue;
				}
			}
			SourceProperties sourceProperties = new SourceProperties();
			updateSourcePropertiesValue(isScreenDest, destContent, sourceProperties);
			List<String> sourceList = new ArrayList<>(sourceIdToNameMap.values());
			populateRouting(stats, groupType, screenDestName, sourceProperties.currentSourceName, sourceList, controls, sourceProperties.numberOfSource);
		}
	}

	/**
	 * Get first normal layer index
	 *
	 * @param layerNode JsonNode
	 * @param firstNormalLayerIndex normal layer index
	 * @return layer index
	 */
	private int getFirstNormalLayerIndex(JsonNode layerNode, int firstNormalLayerIndex) {
		for (int j = 0; j < layerNode.size(); j++) {
			JsonNode currentLayerNode = layerNode.get(j);
			JsonNode linkDestIdNode = currentLayerNode.get(BarcoE2Constant.LINK_DEST_ID);
			if (linkDestIdNode == null) {
				throw new ResourceNotReachableException(String.format("Cannot get layer at index %s", j));
			}
			if (linkDestIdNode.asInt() == -1) {
				firstNormalLayerIndex = j;
				break;
			}
		}
		return firstNormalLayerIndex;
	}

	/**
	 * Routing control: Get JsonNode destination content
	 *
	 * @param isScreenDest boolean for Screen/Aux destination
	 * @param response Input JsonNode
	 * @param index The index of destination in the loop
	 * @return This returns the destination content JsonNode
	 * @throws Exception Throw exception if fail to get content of destination
	 */
	private JsonNode getDestContent(boolean isScreenDest, JsonNode response, int index) throws Exception {
		int destId = response.get(index).get(BarcoE2Constant.ID).asInt();
		if (isScreenDest) {
			return getScreenDestContent(destId);
		}
		return getAuxDestContent(destId);
	}

	/**
	 * Get Aux Destination content
	 *
	 * @param destId Aux destination id
	 * @return JsonNode
	 * @throws Exception Throw exception if fail to get Aux Content
	 */
	private JsonNode getAuxDestContent(int destId) throws Exception {
		Map<Object, Object> destParams = new HashMap<>();
		destParams.put(BarcoE2Constant.ID, destId);
		return requestByMethod(BarcoE2Constant.METHOD_LIST_AUX_CONTENT, destParams);
	}

	/**
	 * Get Screen Destination content
	 *
	 * @param destId Screen destination id
	 * @return JsonNode
	 * @throws Exception Throw exception if fail to get Screen Dest Content
	 */
	private JsonNode getScreenDestContent(int destId) throws Exception {
		Map<Object, Object> destParams = new HashMap<>();
		destParams.put(BarcoE2Constant.ID, destId);
		return requestByMethod(BarcoE2Constant.METHOD_LIST_CONTENT, destParams);
	}

	/**
	 * Routing control: Get routing control json node based on Screen/Aux Destination
	 *
	 * @param methodName Screen/Aux Destination name
	 * @return This returns the JsonNode
	 * @throws Exception Throw exception if fail to get list of destination
	 */
	private JsonNode getRoutingControlJsonNode(String methodName) throws Exception {
		Map<Object, Object> params = new HashMap<>();
		params.put(BarcoE2Constant.TYPE, BarcoE2Constant.SHOW_ALL_DESTINATION);
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS, params);
		// Check if node is null
		if (!NullNode.instance.equals(response.get(methodName)) && response.get(methodName) != null && response.get(methodName).size() != 0) {
			return requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS, params).get(methodName);
		} else {
			return null;
		}
	}

	/**
	 * Routing control: Get all sources of the device.
	 *
	 * @return A map contains source ids and source names.
	 */
	private Map<Integer, String> getSourceIdToSourceNameMap() throws Exception {
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_SOURCES, new HashMap<>());
		if (response == null) {
			throw new ResourceNotReachableException("Cannot get list of source");
		}
		Map<Integer, String> sourceIdAndSourceName = new HashMap<>();
		for (int i = 0; i < response.size(); i++) {
			JsonNode sourceResponse = response.get(i);
			JsonNode sourceNameNode = sourceResponse.get(BarcoE2Constant.NAME);
			JsonNode sourceIdNode = sourceResponse.get(BarcoE2Constant.ID);
			if (sourceNameNode == null || sourceIdNode == null) {
				continue;
			}
			String sourceName = sourceNameNode.asText();
			int sourceId = sourceIdNode.asInt();
			sourceIdAndSourceName.put(sourceId, sourceName);
		}
		if (sourceIdAndSourceName.size() == 0) {
			throw new ResourceNotReachableException("Cannot get list of source");
		}
		sourceIdAndSourceName.put(BarcoE2Constant.NO_RECALLED_PRESET, BarcoE2Constant.NONE);
		// return sorted map of sources by name
		return sourceIdAndSourceName.entrySet()
				.stream()
				.sorted(Map.Entry.comparingByValue())
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(oldValue, newValue) -> oldValue, LinkedHashMap::new));
	}

	/**
	 * Routing control: Populate monitoring/controlling data for screen/aux destination
	 *
	 * @param methodName Type of the destination (ScreenDestination/AuxDestination)
	 * @param screenDestName Name of the destination
	 * @param currentSourceName Current source name
	 * @param sourceList List of sources
	 * @param controls List of AdvancedControllableProperty
	 * @param numberOfSource Number of sources that are assigned to this destination
	 */
	private void populateRouting(Map<String, String> stats, String methodName, String screenDestName, String currentSourceName,
			List<String> sourceList, List<AdvancedControllableProperty> controls, int numberOfSource) {
		stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, screenDestName), currentSourceName);
		controls.add(createDropdown(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, screenDestName), currentSourceName, sourceList));
		if (numberOfSource > 1) {
			stats.put(String.format("%s#%s%s", methodName, screenDestName, BarcoE2Constant.DESTINATION_STATUS), BarcoE2Constant.DESTINATION_MIXED);
		}
	}

	/**
	 * Routing control:  Assign a new source to Screen Destination
	 *
	 * @param screenName Name of Screen Destination
	 * @param sourceName The source that will be assigned to the Screen Destination
	 * @throws Exception Throw exception when fail to call command on the device
	 */
	private void changeScreenContent(String screenName, String sourceName) throws Exception {
		// STEP 1 get screen id
		int currentScreenDestId = getCurrentScreenDestId(screenName);
		if (currentScreenDestId == -1)
		{
			throw new ResourceNotReachableException(String.format("Not found destination with name %s", screenName));
		}
 		// If sourceName = 'None' => clear all source and layers
		if (BarcoE2Constant.NONE.equals(sourceName)) {
			clearSourceFromLayer(currentScreenDestId);
			clearLayerFromDest(currentScreenDestId, -1, false);
			return;
		}
		// STEP 2 check if layer is mixed or single
		JsonNode screenDestContent = getScreenDestContent(currentScreenDestId);
		JsonNode layerNode = screenDestContent.get(BarcoE2Constant.LAYERS);
		int layerIndex = -1;
		layerIndex = getFirstNormalLayerIndex(layerNode, layerIndex);
		if (layerIndex == -1) {
			throw new ResourceNotReachableException(String.format("There is no normal layer in %s", screenName));
		}
		boolean isMixedLayer = checkLayerType(currentScreenDestId, layerIndex);
		// STEP 3 Clear layers:
		clearLayerFromDest(currentScreenDestId, layerIndex, isMixedLayer);
		// STEP 4 changeContent
		int getSourceId = getNewSourceId(sourceName);
		if (getSourceId == -1) {
			throw new CommandFailureException(this.getAddress(), "getSourceId", String.format("There is no source with name %s", sourceName));
		}
		assignToDest(getSourceId, currentScreenDestId, layerIndex, isMixedLayer);
	}

	/**
	 * Check layer type method - mixed/single layer
	 *
	 * @param currentScreenDestId screen dest id
	 * @param layerId id of layer
	 * @return return true if mixed layer, false: single layer
	 * @throws Exception Throw exception when fail to get JsonNode or split string
	 */
	private boolean checkLayerType(int currentScreenDestId, int layerId) throws Exception {
		JsonNode screenDestContent = getScreenDestContent(currentScreenDestId);
		JsonNode layerNode = screenDestContent.get(BarcoE2Constant.LAYERS);
		JsonNode firstLayerNode = layerNode.get(layerId);
		JsonNode secondLayerNode = layerNode.get(layerId + 1);
		String firstLayerName = firstLayerNode.get(BarcoE2Constant.NAME).asText();
		if (firstLayerNode.get(BarcoE2Constant.NAME) == null) {
			throw new ResourceNotReachableException("Cannot get name of layer at index 0");
		}
		if (secondLayerNode == null) {
			return false;
		}
		if (secondLayerNode.get(BarcoE2Constant.NAME) == null) {
			throw new ResourceNotReachableException("Cannot get name of layer at index 1");
		}
		String secondLayerName = secondLayerNode.get(BarcoE2Constant.NAME).asText();
		if (firstLayerName.length() == secondLayerName.length() && firstLayerName.contains(BarcoE2Constant.DASH) && secondLayerName.contains(BarcoE2Constant.DASH)) {
			String[] firstLayerSplit = firstLayerName.split(BarcoE2Constant.DASH);
			String[] secondLayerSplit = secondLayerName.split(BarcoE2Constant.DASH);
			return firstLayerSplit[0].equals(secondLayerSplit[0]);
		}
		return false;
	}

	/**
	 * Clear source from the rest of layer
	 *
	 * @param destId Screen Destination Id
	 * @throws Exception Throw exception when fail to call changeContent/ get JsonNode
	 */
	private void clearSourceFromLayer(int destId) throws Exception {
		JsonNode screenDestContent = getScreenDestContent(destId);
		JsonNode layerNode = screenDestContent.get(BarcoE2Constant.LAYERS);
		for (int i = 0; i < layerNode.size(); i++) {
			JsonNode currentLayer = layerNode.get(i);
			if (currentLayer.get(BarcoE2Constant.ID) == null) {
				throw new ResourceNotReachableException(String.format("Fail to get layer in screen destination with id %S", destId));
			}
			LayerDTO layerDTO = new LayerDTO(currentLayer.get(BarcoE2Constant.ID).asInt(), -1, 0, 0);
			Dimension dimension = populateDimension(destId, false);
			layerDTO.setDimension(dimension);
			int linkDestId = currentLayer.get(BarcoE2Constant.LINK_DEST_ID).asInt();
			// exclude super layer
			if (linkDestId != -1) {
				continue;
			}
			layerDTO.setLinkDestId(linkDestId);
			int linkLayerId = currentLayer.get(BarcoE2Constant.LINK_LAYER_ID).asInt();
			layerDTO.setLinkLayerId(linkLayerId);
			Map<Object, Object> changeScreenParams = new HashMap<>();
			changeScreenParams.put(BarcoE2Constant.ID, destId);
			changeScreenParams.put(BarcoE2Constant.LAYERS, List.of(layerDTO).toArray());
			JsonNode changeScreenContentResponse = requestByMethod(BarcoE2Constant.METHOD_CHANGE_CONTENT, changeScreenParams);
			if (changeScreenContentResponse.get(BarcoE2Constant.SUCCESS_STATUS).asInt() != 0) {
				throw new CommandFailureException(this.getAddress(), "changeContent", "Fail to clear source from layer");
			}
		}
	}

	/**
	 * Routing control: assign new source to screen/super destination.
	 *
	 * @param getSourceIndex new source index
	 * @param currentScreenDestId name of current super/screen destination
	 * @param layerIdx index of layer
	 * @param isMixedLayer check if layer is mixed
	 * @throws Exception Throw exception when fail to changeContent
	 */
	private void assignToDest(int getSourceIndex, int currentScreenDestId, int layerIdx, boolean isMixedLayer) throws Exception {
		// STEP 1 get dimension from source and destination
		Dimension sourceDimension = populateDimension(getSourceIndex, true);
		Dimension destDimension = populateDimension(currentScreenDestId, false);
		// STEP 2 calculate dimension to make the layer stay in the middle of the screen destination
		Dimension layerDimension = calculateProperDimension(sourceDimension, destDimension);
		// STEP 3 put dimension to list
		List<LayerDTO> layerDTOList = new ArrayList<>();
		// GET DEST CONTENT
		JsonNode currenScreenDestContent = getScreenDestContent(currentScreenDestId);
		JsonNode layerNode = currenScreenDestContent.get(BarcoE2Constant.LAYERS);
		if (isMixedLayer) {
			int secondLayerPgmMode = layerNode.get(layerIdx + 1).get(BarcoE2Constant.PGM_MODE).asInt();
			int firstLayerPgmMode = secondLayerPgmMode == 0 ? 1 : 0;
			int firstLayerPvwMode = Math.abs(firstLayerPgmMode - 1);
			int secondLayerPvmMode = Math.abs(secondLayerPgmMode - 1);
			LayerDTO firstLayerDTO = prepareLayerDTO(layerIdx, getSourceIndex, firstLayerPvwMode, firstLayerPgmMode, layerDimension, layerNode);
			layerDTOList.add(firstLayerDTO);
			LayerDTO secondLayerDTO = prepareLayerDTO(layerIdx + 1, getSourceIndex, secondLayerPvmMode, secondLayerPgmMode, layerDimension, layerNode);
			layerDTOList.add(secondLayerDTO);
		} else {
			LayerDTO layerDTO = prepareLayerDTO(layerIdx, getSourceIndex, 1, 1, layerDimension, layerNode);
			layerDTOList.add(layerDTO);
		}
		// STEP 4 prepare param before call changeContent
		Map<Object, Object> changeScreenParams = new HashMap<>();
		changeScreenParams.put(BarcoE2Constant.ID, currentScreenDestId);
		boolean result;
		if (isMixedLayer) {
			changeScreenParams.put(BarcoE2Constant.LAYERS, List.of(layerDTOList.get(0)).toArray());
			boolean assignFirstLayer = requestByMethod(BarcoE2Constant.METHOD_CHANGE_CONTENT, changeScreenParams).get(BarcoE2Constant.SUCCESS_STATUS).asInt() == 0;
			changeScreenParams.remove(BarcoE2Constant.LAYERS);
			changeScreenParams.put(BarcoE2Constant.LAYERS, List.of(layerDTOList.get(1)).toArray());
			boolean assignSecondLayer = requestByMethod(BarcoE2Constant.METHOD_CHANGE_CONTENT, changeScreenParams).get(BarcoE2Constant.SUCCESS_STATUS).asInt() == 0;
			result = assignFirstLayer && assignSecondLayer;
		} else {
			changeScreenParams.put(BarcoE2Constant.LAYERS, layerDTOList.toArray());
			result = requestByMethod(BarcoE2Constant.METHOD_CHANGE_CONTENT, changeScreenParams).get(BarcoE2Constant.SUCCESS_STATUS).asInt() == 0;
		}
		if (!result) {
			throw new CommandFailureException(this.getAddress(), "changeContent", "Fail to assign source to super/screen destination");
		}
	}

	/**
	 * Prepare data for layer DTO
	 *
	 * @param layerId id of layer
	 * @param getSourceIndex source index
	 * @param pvmMode preview mode
	 * @param pgmMode program mode
	 * @param layerDimension layer dimension
	 * @param layerNode layer node
	 * @return LayerDTO
	 */
	private LayerDTO prepareLayerDTO(int layerId, int getSourceIndex, int pvmMode, int pgmMode, Dimension layerDimension, JsonNode layerNode) {
		LayerDTO firstLayerDTO = new LayerDTO(layerId, getSourceIndex, pvmMode, pgmMode);
		firstLayerDTO.setDimension(layerDimension);
		JsonNode firstLayerNode = layerNode.get(layerId);
		int firstLinkLayerId = firstLayerNode.get(BarcoE2Constant.LINK_LAYER_ID).asInt();
		int firstLinkDestId = firstLayerNode.get(BarcoE2Constant.LINK_DEST_ID).asInt();
		firstLayerDTO.setLinkLayerId(firstLinkLayerId);
		firstLayerDTO.setLinkDestId(firstLinkDestId);
		return firstLayerDTO;
	}

	/**
	 * Populate dimension for
	 *
	 * @param id id of array
	 * @param isSourceNode is source node
	 * @throws Exception when fail to get JsonNode
	 */
	private Dimension populateDimension(int id, boolean isSourceNode) throws Exception {
		JsonNode jsonNode;
		if (isSourceNode) {
			jsonNode = requestByMethod(BarcoE2Constant.METHOD_LIST_SOURCES, new HashMap<>());
		} else {
			jsonNode = getRoutingControlJsonNode(BarcoE2Constant.SCREEN_DESTINATION);
		}
		Dimension dimension = new Dimension();
		for (int i = 0; i < (jsonNode != null ? jsonNode.size() : 0); i++) {
			JsonNode currentDestNode = jsonNode.get(i);
			if (currentDestNode.get(BarcoE2Constant.ID) == null) {
				throw new ResourceNotReachableException(String.format("Cannot get screen destination content at index %s", i));
			}
			if (currentDestNode.get(BarcoE2Constant.ID).asInt() == id) {
				dimension.sethSize(currentDestNode.get(BarcoE2Constant.H_SIZE).asInt());
				dimension.setvSize(currentDestNode.get(BarcoE2Constant.V_SIZE).asInt());
				dimension.setvPos(0);
				dimension.sethPos(0);
				// break after get info success
				break;
			}
		}
		return dimension;
	}

	/**
	 * Calculate the dimension to make the layer center
	 *
	 * @param sourceDimension Dimension of the source that will be changed
	 * @param destinationDimension Dimension of destination
	 * @return This returns the new dimension
	 */
	private Dimension calculateProperDimension(Dimension sourceDimension, Dimension destinationDimension) {
		// CASE 1: new dim vsize = dest v size
		Dimension newDimension = new Dimension();
		newDimension.setvSize(destinationDimension.getvSize());
		int newHsize = destinationDimension.getvSize() * sourceDimension.gethSize() / sourceDimension.getvSize();
		newDimension.sethSize(newHsize);
		newDimension.setvPos(0);
		int newHpos = (destinationDimension.gethSize() - newHsize) / 2;
		newDimension.sethPos(newHpos);
		if (newDimension.gethSize() > destinationDimension.gethSize() || newDimension.getvSize() > destinationDimension.getvSize()) {
			// CASE 2:
			newDimension = new Dimension();
			newDimension.sethSize(destinationDimension.gethSize());
			int newVsize = destinationDimension.gethSize() * sourceDimension.getvSize() / sourceDimension.gethSize();
			newDimension.setvSize(newVsize);
			newDimension.sethPos(0);
			int newVPos = (destinationDimension.getvSize() - newVsize) / 2;
			newDimension.setvPos(newVPos);
		}

		return newDimension;
	}

	/**
	 * Routing control: get destination id
	 *
	 * @param screenName name of super/screen destination
	 * @return This returns the id of super/screen destination
	 * @throws Exception Throw exception when fail to get list of destination
	 */
	private int getCurrentScreenDestId(String screenName) throws Exception {
		Map<Object, Object> params = new HashMap<>();
		params.put(BarcoE2Constant.TYPE, BarcoE2Constant.DESTINATION_SCREEN_TYPE);
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS, params).get(BarcoE2Constant.SCREEN_DESTINATION);
		int currentScreenDestId = BarcoE2Constant.NOT_MATCH_SCREEN_ID;
		for (int i = 0; i < response.size(); i++) {
			JsonNode screenResponse = response.get(i);
			if (screenResponse != null && Objects.equals(screenName, screenResponse.get(BarcoE2Constant.NAME).asText())) {
				currentScreenDestId = screenResponse.get(BarcoE2Constant.ID).asInt();
				break;
			}
		}
		return currentScreenDestId;
	}

	/**
	 * Routing control: get aux destination id
	 *
	 * @param auxName name of aux destination
	 * @return This returns the id of aux destination
	 * @throws Exception Throw exception when fail to get list of destination
	 */
	private int getCurrentAuxDestId(String auxName) throws Exception {
		Map<Object, Object> params = new HashMap<>();
		params.put(BarcoE2Constant.TYPE, BarcoE2Constant.DESTINATION_AUX_TYPE);
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS, params).get(BarcoE2Constant.AUX_DESTINATION);
		int currentAuxDestId = BarcoE2Constant.NOT_MATCH_SCREEN_ID;
		for (int i = 0; i < response.size(); i++) {
			JsonNode screenResponse = response.get(i);
			if (screenResponse != null && Objects.equals(auxName, screenResponse.get(BarcoE2Constant.NAME).asText())) {
				currentAuxDestId = screenResponse.get(BarcoE2Constant.ID).asInt();
				break;
			}
		}
		return currentAuxDestId;
	}

	/**
	 * Routing control: Clear layer result
	 *
	 * @param currentScreenDestId screen destination id
	 * @param excludeLayerId exclude id from loop, excludeLayerId = -1 means clear all layer from the destination.
	 * @param isMixedType check if layer is mixed type
	 * @throws Exception throw exception when fail to get screen destination content
	 */
	private void clearLayerFromDest(int currentScreenDestId, int excludeLayerId, boolean isMixedType) throws Exception {
		JsonNode screenDestContent = getScreenDestContent(currentScreenDestId).get(BarcoE2Constant.LAYERS);
		List<LayerRequestDTO> layerRequestDTOList = new ArrayList<>();
		for (int i = 0; i < screenDestContent.size(); i++) {
			JsonNode currentScreenDestContent = screenDestContent.get(i);
			if (currentScreenDestContent.get(BarcoE2Constant.ID) == null) {
				throw new ResourceNotReachableException(String.format("Fail to get layer id in screen destination with id %s", currentScreenDestId));
			}
			int currentLayerID = currentScreenDestContent.get(BarcoE2Constant.ID).asInt();
			if (currentLayerID == excludeLayerId) {
				continue;
			}
			if (isMixedType && currentLayerID == excludeLayerId + 1) {
				continue;
			}
			int linkDestId = currentScreenDestContent.get(BarcoE2Constant.LINK_DEST_ID).asInt();
			if (linkDestId != -1) {
				continue;
			}
			int pvwMode = currentScreenDestContent.get(BarcoE2Constant.PVW_MODE).asInt();
			int pgmMode = currentScreenDestContent.get(BarcoE2Constant.PGM_MODE).asInt();
			if (pvwMode == 1 || pgmMode == 1) {
				layerRequestDTOList.add(new LayerRequestDTO(currentLayerID));
			}
		}
		Map<Object, Object> clearLayerParams = new HashMap<>();
		clearLayerParams.put(BarcoE2Constant.SCREEN_ID, currentScreenDestId);
		clearLayerParams.put(BarcoE2Constant.LAYERS, layerRequestDTOList.toArray());
		JsonNode clearLayerResponse = requestByMethod(BarcoE2Constant.METHOD_CLEAR_LAYERS, clearLayerParams);
		JsonNode clearLayerSuccessNode = clearLayerResponse.get(BarcoE2Constant.SUCCESS_STATUS);
		if (clearLayerSuccessNode == null || clearLayerSuccessNode.asInt() != 0) {
			throw new CommandFailureException(this.getAddress(), "clearLayers", "Fail to call clearLayers");
		}
	}

	/**
	 * Routing control: Get new source index based on source name
	 *
	 * @param sourceName new source name
	 * @return This returns the new source index
	 */
	private int getNewSourceId(String sourceName) {
		int newSourceIndex = -1;
		for (Entry<Integer, String> entry : sourceIdToNameMap.entrySet()) {
			if (entry.getValue().equals(sourceName)) {
				newSourceIndex = entry.getKey();
				break;
			}
		}
		return newSourceIndex;
	}

	/**
	 * Routing control:  Assign a new source to Aux Destination
	 *
	 * @param auxName Name of Aux Destination
	 * @param sourceName The source that will be assigned to the Aux Destination
	 * @throws Exception when fail to changeAuxContent
	 */
	private void changeAuxContent(String auxName, String sourceName) throws Exception {
		// STEP 1 get aux destination id from auxName
		Map<Object, Object> params = new HashMap<>();
		params.put(BarcoE2Constant.TYPE, BarcoE2Constant.DESTINATION_AUX_TYPE);
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS, params).get(BarcoE2Constant.AUX_DESTINATION);
		if (response == null) {
			throw new ResourceNotReachableException("Fail to get list of aux destinations");
		}
		int currentAuxDestId = BarcoE2Constant.NOT_MATCH_AUX_ID;
		for (int i = 0; i < response.size(); i++) {
			JsonNode auxResponse = response.get(i);
			if (auxResponse != null && Objects.equals(auxName, auxResponse.get(BarcoE2Constant.NAME).asText())) {
				currentAuxDestId = auxResponse.get(BarcoE2Constant.ID).asInt();
			}
		}
		// STEP 2 prepare params before calling changeAuxContent
		Map<Object, Object> changeAuxParams = new HashMap<>();
		changeAuxParams.put(BarcoE2Constant.ID, currentAuxDestId);
		// STEP 3 get index of the source that will be assigned to aux destination
		int getSourceIndex = 0;
		for (Entry<Integer, String> entry : sourceIdToNameMap.entrySet()) {
			if (entry.getValue().equals(sourceName)) {
				getSourceIndex = entry.getKey();
				break;
			}
		}
		changeAuxParams.put(BarcoE2Constant.PVM_LAST_SRC_INDEX, getSourceIndex);
		changeAuxParams.put(BarcoE2Constant.PGM_LAST_SRC_INDEX, getSourceIndex);
		// STEP 4 call changeAuxContent
		JsonNode changeAuxContentResponse = requestByMethod(BarcoE2Constant.METHOD_CHANGE_AUX_CONTENT, changeAuxParams);
		if (changeAuxContentResponse == null || changeAuxContentResponse.get(BarcoE2Constant.SUCCESS_STATUS).asInt() != 0) {
			throw new CommandFailureException(this.getAddress(), "changeAuxContent", "Fail to call changeAuxContent");
		}
	}

	/**
	 * Routing control: Get routing for super dest/aux
	 *
	 * @param isSuperDest boolean true/false if destination is screen/aux destination
	 * @param stats Map of stats
	 * @param controls List of AdvancedControllableProperty
	 * @throws Exception Throw exception if fail to get json node,
	 */
	private void getSuperRoutingControl(boolean isSuperDest, Map<String, String> stats, List<AdvancedControllableProperty> controls) throws Exception {
		String methodName = isSuperDest ? BarcoE2ControllingMetric.SUPER_SCREEN_DESTINATION.getName() : BarcoE2ControllingMetric.SUPER_AUX_DESTINATION.getName();
		List<Integer> listDestIds = isSuperDest ? listSuperDestId : listSuperAuxId;
		if (listDestIds.isEmpty()) {
			return;
		}
		List<String> sourceList = new ArrayList<>(sourceIdToNameMap.values());
		// Super Screen/Aux dest loop
		for (Integer listDestId : listDestIds) {
			JsonNode response = getSuperDestContent(isSuperDest, String.valueOf(listDestId));
			if (response == null) {
				continue;
			}
			JsonNode successStatusNode = response.get(BarcoE2Constant.SUCCESS_STATUS);
			if (successStatusNode != null && successStatusNode.asInt() == -1) {
				if (logger.isDebugEnabled() && response.get(BarcoE2Constant.RESPONSE) != null) {
					logger.debug(response.get(BarcoE2Constant.RESPONSE));
				}
				continue;
			}
			populateSuperRouting(stats, controls, sourceList, methodName, response, isSuperDest);
		}
	}

	/**
	 * Routing control: Get list of super screen/aux destination ids
	 *
	 * @param isSuperDest boolean true/false based on super destination/ super aux destination
	 * @return List of ids
	 */
	private List<Integer> handleListSuperId(boolean isSuperDest) {
		try {
			List<Integer> resultList = new ArrayList<>();
			if (isSuperDest) {
				// Add string to List directly if length is 1
				if (this.getListSuperScreenDestId().length() == 1) {
					int currentIntValue = Integer.parseInt(this.getListSuperScreenDestId());
					if (currentIntValue >= 0) {
						resultList.add(currentIntValue);
					}
				} else {
					// Split string array of IDs and populate to List
					String[] listIds = this.getListSuperScreenDestId().split(BarcoE2Constant.COMMA);
					for (String listId : listIds) {
						int currentIntValue = Integer.parseInt(listId);
						if (currentIntValue >= 0) {
							resultList.add(currentIntValue);
						}
					}
				}
			} else {
				// Add string to List directly if length is 1
				if (this.getListSuperAuxDestId().length() == 1) {
					int currentIntValue = Integer.parseInt(this.getListSuperAuxDestId());
					if (currentIntValue >= 0) {
						resultList.add(currentIntValue);
					}
				} else {
					// Split string array of IDs and populate to List
					String[] listIds = this.getListSuperAuxDestId().split(BarcoE2Constant.COMMA);
					for (String listId : listIds) {
						int currentIntValue = Integer.parseInt(listId);
						if (currentIntValue >= 0) {
							resultList.add(currentIntValue);
						}
					}
				}
			}
			return resultList;
		} catch (Exception e) {
			throw new IllegalArgumentException("Fail to parse the string to integer, input from adapter properties is wrong", e);
		}
	}

	/**
	 * Routing control: Get super routing control json node based on Super Screen/Aux Destination
	 *
	 * @param isSuperDest boolean true/false based on super destination/ super aux destination
	 * @param superDestId id of super dest/aux destination
	 * @return This returns the JsonNode
	 * @throws Exception Throw exception if fail to get list of destination
	 */
	private JsonNode getSuperDestContent(boolean isSuperDest, String superDestId) throws Exception {
		if (this.getListSuperAuxDestId() != null || this.getListSuperScreenDestId() != null) {
			Map<Object, Object> params = new HashMap<>();
			JsonNode response;
			if (isSuperDest) {
				params.put(BarcoE2Constant.ID, Integer.parseInt(superDestId));
				response = requestByMethod(BarcoE2Constant.METHOD_LIST_SUPER_DEST_CONTENT, params);
			} else {
				params.put(BarcoE2Constant.ID, Integer.parseInt(superDestId));
				response = requestByMethod(BarcoE2Constant.METHOD_LIST_SUPER_AUX_CONTENT, params);
			}
			// Check if node is null
			if (!NullNode.instance.equals(response) && response != null) {
				return response;
			}
		}
		return null;
	}

	/**
	 * Routing control: Populate monitoring/controlling data for super screen/aux destination
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @param sourceList List of sources
	 * @param methodName Type of the destination (Super ScreenDestination/AuxDestination)
	 * @param response JsonNode of Super Screen/Aux Destination
	 * @param isScreenDest true/false based on Super Destination/Aux Destination
	 * @throws Exception if fail to get Screen/Aux destination content, fail to update SourceProperties
	 */
	private void populateSuperRouting(Map<String, String> stats, List<AdvancedControllableProperty> controls, List<String> sourceList,
			String methodName, JsonNode response, boolean isScreenDest) throws Exception {
		if (isScreenDest) {
			populateSuperDestinationData(stats, controls, sourceList, methodName, response);
		} else {
			populateSuperAuxDestinationData(stats, controls, sourceList, methodName, response);
		}
	}

	/**
	 * Populate monitoring data for super aux destination
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @param sourceList list of source
	 * @param methodName method name (super/aux destination)
	 * @param response JsonNode response
	 * @throws Exception Throw exception when fail to get Aux content
	 */
	private void populateSuperAuxDestinationData(Map<String, String> stats, List<AdvancedControllableProperty> controls, List<String> sourceList, String methodName, JsonNode response) throws Exception {
		// STEP 1: Get super aux destination DTO
		SuperAuxDestination superAuxDestination;
		try {
			superAuxDestination = (SuperAuxDestination) jsonNodeToDTO(response, SuperAuxDestination.class);
		} catch (Exception e) {
			return;
		}
		if (superAuxDestination.getAuxDestinationList().isEmpty()) {
			return;
		}
		List<AuxDestination> auxDestinationList = superAuxDestination.getAuxDestinationList();
		for (AuxDestination auxDestination : auxDestinationList) {
			// STEP 2 update number of source and number and current assigned source name
			SourceProperties sourceProperties = new SourceProperties();
			int auxId = getCurrentAuxDestId(auxDestination.getName());
			JsonNode auxDestContent = getAuxDestContent(auxId);
			updateSourcePropertiesValue(false, auxDestContent, sourceProperties);
			if (Objects.equals(sourceProperties.currentSourceName, BarcoE2Constant.DOUBLE_QUOTES)) {
				sourceProperties.currentSourceName = BarcoE2Constant.NONE;
			}
			String groupNameAndSuperAuxName = methodName + BarcoE2Constant.COLON + superAuxDestination.getName();
			stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, groupNameAndSuperAuxName, auxDestination.getName()), sourceProperties.currentSourceName);
			controls.add(createDropdown(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, groupNameAndSuperAuxName, auxDestination.getName()),
					sourceProperties.currentSourceName, sourceList));
		}
	}

	/**
	 * Populate monitoring data for super destination
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @param sourceList list of source
	 * @param methodName method name (super/aux destination)
	 * @param response JsonNode response
	 * @throws Exception Throw exception when fail to get screen destination content
	 */
	private void populateSuperDestinationData(Map<String, String> stats, List<AdvancedControllableProperty> controls, List<String> sourceList, String methodName, JsonNode response) throws Exception {
		// STEP 1: Get super destination DTO
		SuperDestination superDestination;
		try {
			superDestination = (SuperDestination) jsonNodeToDTO(response, SuperDestination.class);
		} catch (Exception e) {
			return;
		}
		if (superDestination.getListScreenDestName().isEmpty()) {
			return;
		}
		List<String> screenDestNameList = superDestination.getListScreenDestName();
		for (String destName : screenDestNameList) {
			// STEP 2 update number of source and number and current assigned source name
			SourceProperties sourceProperties = new SourceProperties();
			int screenId = getCurrentScreenDestId(destName);
			JsonNode screenDestContent = getScreenDestContent(screenId);
			updateSourcePropertiesValue(true, screenDestContent, sourceProperties);
			String groupNameAndSuperDestName = methodName + BarcoE2Constant.COLON + superDestination.getName();
			JsonNode layerNode = screenDestContent.get(BarcoE2Constant.LAYERS);
			if (layerNode == null) {
				continue;
			}
			int haveNormalLayer = -1;
			for (int j = 0; j < layerNode.size(); j++) {
				JsonNode currentLayerNode = layerNode.get(j);
				JsonNode linkDestIdNode = currentLayerNode.get(BarcoE2Constant.LINK_DEST_ID);
				if (linkDestIdNode == null) {
					throw new ResourceNotReachableException(String.format("Cannot get layer at index %s", j));
				}
				if (linkDestIdNode.asInt() == -1) {
					haveNormalLayer = j;
					break;
				}
			}
			if (haveNormalLayer == -1) {
				stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, groupNameAndSuperDestName, destName), BarcoE2Constant.NOT_FOUND_LAYER);
				continue;
			}
			stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, groupNameAndSuperDestName, destName), sourceProperties.currentSourceName);
			controls.add(createDropdown(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, groupNameAndSuperDestName, destName),
					sourceProperties.currentSourceName, sourceList));
		}
	}

	/**
	 * Create a button.
	 *
	 * @param name name of the button
	 * @param label label of the button
	 * @param labelPressed label of the button after pressing it
	 * @param gracePeriod grace period of button
	 * @return This returns the instance of {@link AdvancedControllableProperty} type Button.
	 */
	private AdvancedControllableProperty createButton(String name, String label, String labelPressed, long gracePeriod) {
		AdvancedControllableProperty.Button button = new AdvancedControllableProperty.Button();
		button.setLabel(label);
		button.setLabelPressed(labelPressed);
		button.setGracePeriod(gracePeriod);
		return new AdvancedControllableProperty(name, new Date(), button, BarcoE2Constant.DOUBLE_QUOTES);
	}

	/**
	 * Create dropdown
	 *
	 * @param name name of the dropdown
	 * @param initialValue initial value of the dropdown
	 * @param options List of options
	 * @return This returns the instance of {@link AdvancedControllableProperty} type Dropdown.
	 */
	private AdvancedControllableProperty createDropdown(String name, String initialValue, List<String> options) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(options.toArray(new String[0]));
		dropDown.setLabels(options.toArray(new String[0]));
		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}
}

