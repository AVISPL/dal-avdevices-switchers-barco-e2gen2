/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
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
		listSuperDestId = handleListSuperId(true);
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
		listSuperAuxId = handleListSuperId(false);
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
			case SCREEN_DESTINATION:
				controlAssignSourceToScreenDest(value, propertyValue);
				break;
			case SUPER_SCREEN_DESTINATION:
				controlAssignSourceToSuperDest(value, propertyValue);
				break;
			case AUX_DESTINATION:
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
			logger.debug(String.format("Perform doPost() at host %s with port %s", this.host, this.getPort()));
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
	 *
	 * @throws Exception Throw exception when fail to get list source
	 */
	private void prepareDeviceMetaData() throws Exception {
		sourceIdToNameMap = getSourceIdToSourceNameMap();
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
		prepareDeviceMetaData();
		getDeviceInformation(statistics);
		getPresetFeedBack(statistics, controls);
		getRoutingControl(true, statistics, controls);
		getRoutingControl(false, statistics, controls);
		getSuperRoutingControl(true, statistics, controls);
		getSuperRoutingControl(false, statistics, controls);
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
			String auxDestName = getAuxDestinationName(destName);
			changeAuxContent(auxDestName, sourceName);
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
	private void controlAssignSourceToAuxDest(String destName, String sourceName) throws Exception {
		try {
			changeAuxContent(sourceName, destName);
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
	private void controlAssignSourceToScreenDest(String destName, String sourceName) throws Exception {
		try {
			changeScreenContent(sourceName, destName);
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
	private void controlAssignSourceToSuperDest(String destName, String sourceName) throws Exception {
		try {
			changeSuperScreenContent(sourceName, destName);
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
	 * Get Aux destination name from SuperAux-AuxDestination String
	 *
	 * @param propertyValue String SuperAuxName-AuxName
	 */
	private String getAuxDestinationName(String propertyValue) {
		try {
			String[] splitString = propertyValue.split(BarcoE2Constant.DASH);
			return splitString[1];
		} catch (Exception e) {
			throw new IllegalArgumentException("Fail to split the aux destination name from superAuxDest-auxDest", e);
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
		if (propertyMethod.equals(BarcoE2ControllingMetric.SCREEN_DESTINATION.getName()) || propertyMethod.equals(BarcoE2ControllingMetric.AUX_DESTINATION.getName())
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
		if (response.get(BarcoE2Constant.RESPONSE) != null && response.get(BarcoE2Constant.RESPONSE).isEmpty()) {
			return Collections.emptyList();
		}
		for (int i = 0; i < response.size(); i++) {
			JsonNode responseNodeName = response.get(i);
			if (responseNodeName != null && responseNodeName.get(BarcoE2Constant.NAME) != null) {
				dropDownOptions.add(response.get(i).get(BarcoE2Constant.NAME).asText());
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
			if (recallPresetResponse.get(BarcoE2Constant.RESPONSE) != null) {
				response = recallPresetResponse.get(BarcoE2Constant.RESPONSE);
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
		if (presetResponse.get(BarcoE2Constant.RESPONSE) != null && presetResponse.get(BarcoE2Constant.RESPONSE).isEmpty()) {
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
		Map<Object, Object> param = new HashMap<>();
		param.put(BarcoE2Constant.PRESET_NAME, presetName);
		param.put(BarcoE2Constant.TYPE, BarcoE2Constant.ACTIVE_PRESET_ON_PROGRAM);
		JsonNode activatePresetResponse = requestByMethod(BarcoE2Constant.METHOD_ACTIVATE_PRESET, param);
		if (activatePresetResponse.get(BarcoE2Constant.SUCCESS_STATUS) == null) {
			throw new CommandFailureException(this.getAddress(), "activatePreset", "Fail to activatePreset");
		}
		return activatePresetResponse.get(BarcoE2Constant.SUCCESS_STATUS).asInt() == 0;
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
		sourceProperties.numberOfSource = 1;
		if (lastSrcIndex == -1) {
			sourceProperties.currentSourceName = BarcoE2Constant.NONE;
		} else {
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
			int sourceIndex = layers.get(j).get(BarcoE2Constant.LAST_SRC_IDX).asInt();
			if (sourceIndex != -1) {
				listSourceIndexes.add(sourceIndex);
			}
		}
		Set<Integer> uniqueList = new HashSet<>(listSourceIndexes);
		sourceProperties.numberOfSource = uniqueList.size();
		if (!listSourceIndexes.isEmpty()) {
			sourceProperties.currentSourceName = sourceIdToNameMap.get(listSourceIndexes.get(0));
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
		String methodName = isScreenDest ? BarcoE2ControllingMetric.SCREEN_DESTINATION.getName() : BarcoE2ControllingMetric.AUX_DESTINATION.getName();
		JsonNode response = getRoutingControlJsonNode(methodName);
		if (response == null) {
			return;
		}
		// Screen/Aux dest loop
		for (int i = 0; i < response.size(); i++) {
			int destId = response.get(i).get(BarcoE2Constant.ID).asInt();
			String screenDestName = response.get(i).get(BarcoE2Constant.NAME).asText();
			JsonNode destContent = getDestContent(isScreenDest, response, destId);
			if (isScreenDest) {
				JsonNode layers = destContent.get(BarcoE2Constant.LAYERS);
				if (layers == null || layers.size() == 0) {
					stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, screenDestName), BarcoE2Constant.NOT_FOUND_LAYER);
					return;
				}
			}
			SourceProperties sourceProperties = new SourceProperties();
			updateSourcePropertiesValue(isScreenDest, destContent, sourceProperties);
			List<String> sourceList = new ArrayList<>(sourceIdToNameMap.values());
			populateRouting(stats, methodName, screenDestName, sourceProperties.currentSourceName, sourceList, controls, sourceProperties.numberOfSource);
		}
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
	 * @throws Exception Throw exception when fail to get JsonNode
	 */
	private Map<Integer, String> getSourceIdToSourceNameMap() throws Exception {
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_SOURCES, new HashMap<>());
		Map<Integer, String> sourceIdAndSourceName = new HashMap<>();
		for (int i = 0; i < response.size(); i++) {
			JsonNode sourceResponse = response.get(i);
			String sourceName = sourceResponse.get(BarcoE2Constant.NAME).asText();
			int sourceId = response.get(i).get(BarcoE2Constant.ID).asInt();
			sourceIdAndSourceName.put(sourceId, sourceName);
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
		if (Objects.equals(currentSourceName, BarcoE2Constant.DOUBLE_QUOTES)) {
			currentSourceName = BarcoE2Constant.NONE;
		}
		stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, screenDestName), currentSourceName);
		controls.add(createDropdown(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, screenDestName), currentSourceName, sourceList));
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("%s and %s", screenDestName, numberOfSource));
		}
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
		// If sourceName = 'None' => clear all source and layers
		if (BarcoE2Constant.NONE.equals(sourceName)) {
			clearSourceFromLayer(currentScreenDestId);
			clearLayerFromDest(currentScreenDestId, -1, false);
			return;
		}
		// STEP 2 check if layer is mixed or single
		boolean isMixedLayer = checkLayerType(currentScreenDestId);
		// STEP 3 Clear layers:
		clearLayerFromDest(currentScreenDestId, 0, isMixedLayer);
		// STEP 4 changeContent
		int getSourceIndex = getNewSourceIndex(sourceName);
		assignToDest(getSourceIndex, currentScreenDestId, 0, isMixedLayer);
	}

	/**
	 * Check layer type method - mixed/single layer
	 *
	 * @param currentScreenDestId screen dest id
	 * @return return true if mixed layer, false: single layer
	 * @throws Exception Throw exception when fail to get JsonNode or split string
	 */
	private boolean checkLayerType(int currentScreenDestId) throws Exception {
		JsonNode screenDestContent = getScreenDestContent(currentScreenDestId);
		JsonNode layerNode = screenDestContent.get(BarcoE2Constant.LAYERS);
		String firstLayerName = layerNode.get(0).get(BarcoE2Constant.NAME).asText();
		if (layerNode.get(0).get(BarcoE2Constant.NAME) == null) {
			throw new ResourceNotReachableException("Cannot get name of layer at index 0");
		}
		if (layerNode.get(1) == null) {
			return false;
		}
		if (layerNode.get(1).get(BarcoE2Constant.NAME) == null) {
			throw new ResourceNotReachableException("Cannot get name of layer at index 1");
		}
		String secondLayerName = layerNode.get(1).get(BarcoE2Constant.NAME).asText();
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
	 * @param layerId layer id
	 * @param isMixedLayer check if layer is mixed
	 * @throws Exception Throw exception when fail to changeContent
	 */
	private void assignToDest(int getSourceIndex, int currentScreenDestId, int layerId, boolean isMixedLayer) throws Exception {
		// STEP 1 get dimension from source and destination
		Dimension sourceDimension = populateDimension(getSourceIndex, true);
		Dimension destDimension = populateDimension(currentScreenDestId, false);
		// STEP 2 calculate dimension to make the layer stay in the middle of the screen destination
		Dimension layerDimension = calculateProperDimension(sourceDimension, destDimension);
		// STEP 3 put dimension to list
		List<LayerDTO> layerDTOList = new ArrayList<>();
		if (isMixedLayer) {
			LayerDTO firstLayerDTO = new LayerDTO(layerId, getSourceIndex, 1, 0);
			firstLayerDTO.setDimension(layerDimension);
			layerDTOList.add(firstLayerDTO);
			LayerDTO secondLayerDTO = new LayerDTO(layerId + 1, getSourceIndex, 0, 1);
			secondLayerDTO.setDimension(layerDimension);
			layerDTOList.add(secondLayerDTO);
		} else {
			LayerDTO layerDTO = new LayerDTO(layerId, getSourceIndex, 1, 1);
			layerDTO.setDimension(layerDimension);
			layerDTOList.add(layerDTO);
		}
		// STEP 4 prepare param before call changeContent
		Map<Object, Object> changeScreenParams = new HashMap<>();
		changeScreenParams.put(BarcoE2Constant.ID, currentScreenDestId);
		changeScreenParams.put(BarcoE2Constant.LAYERS, layerDTOList.toArray());
		JsonNode changeScreenContentResponse = requestByMethod(BarcoE2Constant.METHOD_CHANGE_CONTENT, changeScreenParams);
		if (changeScreenContentResponse.get(BarcoE2Constant.SUCCESS_STATUS) == null || changeScreenContentResponse.get(BarcoE2Constant.SUCCESS_STATUS).asInt() != 0) {
			throw new CommandFailureException(this.getAddress(), "changeContent", "Fail to assign source to super/screen destination");
		}
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
			jsonNode = getRoutingControlJsonNode(BarcoE2ControllingMetric.SCREEN_DESTINATION.getName());
		}
		Dimension dimension = new Dimension();
		for (int i = 0; i < (jsonNode != null ? jsonNode.size() : 0); i++) {
			JsonNode currentDestNode = jsonNode.get(i);
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
		Dimension newDimension = new Dimension();
		newDimension.setvSize(destinationDimension.getvSize());
		int newHsize = destinationDimension.getvSize() * sourceDimension.gethSize() / sourceDimension.getvSize();
		newDimension.sethSize(newHsize);
		newDimension.setvPos(0);
		int newHpos = (destinationDimension.gethSize() - newHsize) / 2;
		newDimension.sethPos(newHpos);
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
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS, params).get(BarcoE2ControllingMetric.SCREEN_DESTINATION.getName());
		int currentScreenDestId = BarcoE2Constant.NOT_MATCH_SCREEN_ID;
		for (int i = 0; i < response.size(); i++) {
			JsonNode screenResponse = response.get(i);
			if (screenResponse != null && Objects.equals(screenName, screenResponse.get(BarcoE2Constant.NAME).asText())) {
				currentScreenDestId = response.get(i).get(BarcoE2Constant.ID).asInt();
				break;
			}
		}
		return currentScreenDestId;
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
			if (screenDestContent.get(i).get(BarcoE2Constant.ID) == null) {
				throw new ResourceNotReachableException(String.format("Fail to get layer id in screen destination with id %s", currentScreenDestId));
			}
			int currentLayerID = screenDestContent.get(i).get(BarcoE2Constant.ID).asInt();
			if (currentLayerID == excludeLayerId) {
				continue;
			}
			if (isMixedType && currentLayerID == excludeLayerId + 1) {
				continue;
			}
			layerRequestDTOList.add(new LayerRequestDTO(currentLayerID));
		}
		Map<Object, Object> clearLayerParams = new HashMap<>();
		clearLayerParams.put(BarcoE2Constant.SCREEN_ID, currentScreenDestId);
		clearLayerParams.put(BarcoE2Constant.LAYERS, layerRequestDTOList.toArray());
		JsonNode clearLayerResponse = requestByMethod(BarcoE2Constant.METHOD_CLEAR_LAYERS, clearLayerParams);
		if (clearLayerResponse.get(BarcoE2Constant.SUCCESS_STATUS) == null || clearLayerResponse.get(BarcoE2Constant.SUCCESS_STATUS).asInt() != 0) {
			throw new CommandFailureException(this.getAddress(), "clearLayers", "Fail to call clearLayers");
		}
	}

	/**
	 * Routing control:  Assign a new source to Super Screen Destination
	 *
	 * @param screenName Name of Super Screen Destination
	 * @param sourceName The source that will be assigned to the Super Screen Destination
	 * @throws Exception when fail to get destination content
	 */
	private void changeSuperScreenContent(String screenName, String sourceName) throws Exception {
		// STEP 1:  From superScreenDestination => get global layer map and list of screen destination
		// Exception are thrown when there's no global layer & no collection of screen destination
		SuperDestination superDestination = populateSuperDestination(screenName);
		// STEP 2: From screen dest name list => get id of screen destination that has the largest area.
		int currentScreenDestId = getDestHasMaxAreaId(superDestination);
		// STEP 3: From screen dest id =>  getScreenDestContent
		JsonNode currenScreenDestContent = getScreenDestContent(currentScreenDestId);
		final JsonNode layerNode = currenScreenDestContent.get(BarcoE2Constant.LAYERS);
		int globalLayerId = -1;
		for (int i = 0; i < layerNode.size(); i++) {
			JsonNode currentLayer = layerNode.get(i);
			if (currentLayer != null && superDestination.getId() == currentLayer.get(BarcoE2Constant.LINK_DEST_ID).asInt()) {
				globalLayerId = currentLayer.get(BarcoE2Constant.ID).asInt();
				break;
			}
		}
		boolean isSuccess;
		int getSourceIndex = getNewSourceIndex(sourceName);
		boolean isMixedLayer = checkLayerType(currentScreenDestId);
		if (globalLayerId == -1) {
			// CASE 1 assign to first layer
			isSuccess = assignAndVerifySource(currentScreenDestId, getSourceIndex, isMixedLayer, 0);
		} else {
			// CASE 2 assign to globalLayerId's layer
			isSuccess = assignAndVerifySource(currentScreenDestId, getSourceIndex, isMixedLayer, globalLayerId);
			if (!isSuccess) {
				// retry 1 more time on layer 0
				 isSuccess = assignAndVerifySource(currentScreenDestId, getSourceIndex, isMixedLayer, 0);
			}
		}
		if (isSuccess) return;
		throw new CommandFailureException(this.getAddress(), "changeContent", String.format("Fail to call changeContent on super destination: %s", screenName));
	}

	/**
	 * Assign a source to destination and verify if the data is changed.
	 *
	 * @param currentScreenDestId Screen destination id
	 * @param getSourceIndex new source index that will be assigned
	 * @param isMixedLayer is mixed layer or single layer
	 * @param layerIndex index of layer
	 * @return This returns boolean indicates that success/fail to assign source
	 * @throws Exception Throws exception when fail call post in the device
	 */
	private boolean assignAndVerifySource(int currentScreenDestId, int getSourceIndex, boolean isMixedLayer, int layerIndex) throws Exception {
		boolean isSuccess;
		clearLayerFromDest(currentScreenDestId, layerIndex, isMixedLayer);
		assignToDest(getSourceIndex, currentScreenDestId, layerIndex, isMixedLayer);
		// Sleep here to wait for the device to assign a source.
		Thread.sleep(1000);
		JsonNode screenContentAfterAssign = getScreenDestContent(currentScreenDestId).get(BarcoE2Constant.LAYERS).get(layerIndex);
		isSuccess = isNewSourceAssigned(getSourceIndex, screenContentAfterAssign);
		return isSuccess;
	}

	/**
	 * Populate data for super destination DTO
	 *
	 * @param screenName name of super destination
	 * @return This returns Super Destination DTO
	 * @throws Exception Throw exception when fail to get JsonNode
	 */
	private SuperDestination populateSuperDestination(String screenName) throws Exception {
		SuperDestination superDestination = new SuperDestination();
		for (Integer integer : listSuperDestId) {
			JsonNode superDestContent = getSuperDestContent(true, String.valueOf(integer));
			if (superDestContent == null) {
				throw new CommandFailureException(this.getAddress(), "listSuperDestContent", "Fail to call listSuperDestContent");
			}
			if (screenName.equals(superDestContent.get(BarcoE2Constant.NAME).asText())) {
				superDestination = (SuperDestination) jsonNodeToDTO(superDestContent, SuperDestination.class);
				break;
			} else {
				throw new IllegalArgumentException("Id of super destination from adapter properties don't match the id of routing super destination id");
			}
		}
		if (superDestination.getGlobalLayerMap().isEmpty()) {
			throw new ResourceNotReachableException("Global layer is required to assign a source to super destination");
		}
		if (superDestination.getListScreenDestName().isEmpty()) {
			throw new ResourceNotReachableException("There is no screen destination that is assigned to this super destination");
		}
		return superDestination;
	}

	/**
	 * Check if the new source is assigned to the destination
	 *
	 * @param sourceId id of the source
	 * @param newSourceContent content of the new source
	 * @return This returns the status of assigning a source to destination
	 */
	private boolean isNewSourceAssigned(int sourceId, JsonNode newSourceContent) {
		return newSourceContent.get(BarcoE2Constant.LAST_SRC_IDX).asInt() == sourceId;
	}

	/**
	 * Compare area of destination in screen destination list
	 *
	 * @param superDestination SuperDestination DTO
	 * @return ID of the screen destination that has the largest area
	 * @throws Exception Throw exception when fail to get JsonNode
	 */
	private int getDestHasMaxAreaId(SuperDestination superDestination) throws Exception {
		List<String> screenDestString = superDestination.getListScreenDestName();
		Map<Integer, Integer> mapAreaOfDest = new HashMap<>();
		for (String currentScreenDestName : screenDestString) {
			int screenDestId = getCurrentScreenDestId(currentScreenDestName);
			JsonNode destNode = getRoutingControlJsonNode(BarcoE2ControllingMetric.SCREEN_DESTINATION.getName());
			if (destNode == null) {
				throw new ResourceNotReachableException("Fail to get list of screen destinations");
			}
			for (int j = 0; j < destNode.size(); j++) {
				JsonNode currentDestNode = destNode.get(j);
				if (currentDestNode.get(BarcoE2Constant.ID).asInt() == screenDestId) {
					int hSize = destNode.get(j).get(BarcoE2Constant.H_SIZE).asInt();
					int vSize = destNode.get(j).get(BarcoE2Constant.V_SIZE).asInt();
					int destArea = hSize * vSize;
					mapAreaOfDest.put(destArea, screenDestId);
					break;
				}
			}
		}
		int maxArea = mapAreaOfDest.keySet().stream().max(Integer::compareTo).orElse(0);
		return mapAreaOfDest.get(maxArea);
	}

	/**
	 * Routing control: Get new source index based on source name
	 *
	 * @param sourceName new source name
	 * @return This returns the new source index
	 */
	private int getNewSourceIndex(String sourceName) {
		int newSourceIndex = 0;
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
		JsonNode response = requestByMethod(BarcoE2Constant.METHOD_LIST_DESTINATIONS, params).get(BarcoE2ControllingMetric.AUX_DESTINATION.getName());
		if (response == null) {
			throw new ResourceNotReachableException("Fail to get list of aux destinations");
		}
		int currentAuxDestId = BarcoE2Constant.NOT_MATCH_AUX_ID;
		for (int i = 0; i < response.size(); i++) {
			JsonNode auxResponse = response.get(i);
			if (auxResponse != null && Objects.equals(auxName, auxResponse.get(BarcoE2Constant.NAME).asText())) {
				currentAuxDestId = response.get(i).get(BarcoE2Constant.ID).asInt();
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
			JsonNode response;
			response = getSuperDestContent(isSuperDest, String.valueOf(listDestId));
			if (response == null) {
				return;
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
		if (this.getListSuperScreenDestId() != null || this.getListSuperAuxDestId() != null) {
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
				// stream the string List and convert to integer List
				return resultList;
			} catch (Exception e) {
				throw new IllegalArgumentException("Fail to parse the string to integer, input from adapter properties is wrong", e);
			}
		}
		return Collections.emptyList();
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
			throw new ResourceNotReachableException("Cannot get super aux destination with id given in adapter properties", e);
		}
		if (superAuxDestination.getAuxDestinationList().isEmpty()) {
			return;
		}
		List<AuxDestination> auxDestinationList = superAuxDestination.getAuxDestinationList();
		int numberOfSourceInSuperAuxDest = 0;
		for (AuxDestination auxDestination : auxDestinationList) {
			// STEP 2 update number of source and number and current assigned source name
			SourceProperties sourceProperties = new SourceProperties();
			JsonNode auxDestContent = getAuxDestContent(auxDestination.getId());
			updateSourcePropertiesValue(false, auxDestContent, sourceProperties);
			stats.put(String.format(BarcoE2Constant.GROUP_DEST_HASH_TAG_DEST_NAME_COLON_MEMBER, methodName, superAuxDestination.getName(), auxDestination.getName()), sourceProperties.currentSourceName);
			if (Objects.equals(sourceProperties.currentSourceName, BarcoE2Constant.DOUBLE_QUOTES)) {
				sourceProperties.currentSourceName = BarcoE2Constant.NONE;
			}
			controls.add(createDropdown(String.format(BarcoE2Constant.GROUP_DEST_HASH_TAG_DEST_NAME_COLON_MEMBER, methodName, superAuxDestination.getName(), auxDestination.getName()),
					sourceProperties.currentSourceName, sourceList));
			numberOfSourceInSuperAuxDest += sourceProperties.numberOfSource;
		}
		if (numberOfSourceInSuperAuxDest > 1) {
			stats.put(String.format("%s#%s%s", methodName, superAuxDestination.getName(), BarcoE2Constant.DESTINATION_STATUS), BarcoE2Constant.DESTINATION_MIXED);
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
			throw new ResourceNotReachableException("Cannot get super destination with id given in adapter properties", e);
		}
		if (superDestination.getGlobalLayerMap().isEmpty()) {
			stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, superDestination.getName()), BarcoE2Constant.NOT_FOUND_LAYER);
			return;
		}
		if (superDestination.getListScreenDestName().isEmpty()) {
			stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, superDestination.getName()), BarcoE2Constant.NOT_FOUND_LAYER);
			return;
		}
		// Default value will be the first screen destination's source
		String firstScreenDest = superDestination.getListScreenDestName().get(0);
		int currentScreenDestId = getCurrentScreenDestId(firstScreenDest);
		// STEP 2 update number of source and number and current assigned source name
		SourceProperties sourceProperties = new SourceProperties();
		JsonNode screenDestContent = getScreenDestContent(currentScreenDestId);
		updateSourcePropertiesValue(true, screenDestContent, sourceProperties);
		stats.put(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, superDestination.getName()), superDestination.getListScreenDestName().get(0));
		controls.add(createDropdown(String.format(BarcoE2Constant.GROUP_HASH_TAG_MEMBER, methodName, superDestination.getName()),
				sourceProperties.currentSourceName, sourceList));
		if (Objects.equals(sourceProperties.currentSourceName, BarcoE2Constant.DOUBLE_QUOTES)) {
			sourceProperties.currentSourceName = BarcoE2Constant.NONE;
		}
		int numberOfSource = getNumberOfSourceOfSuperDest(superDestination);
		if (numberOfSource > 1) {
			stats.put(String.format("%s#%s%s", methodName, superDestination.getName(), BarcoE2Constant.DESTINATION_STATUS), BarcoE2Constant.DESTINATION_MIXED);
		}
	}

	/**
	 * Get number of source for super destination
	 *
	 * @param superDestination Super Destination DTO
	 * @return This returns number of sources
	 * @throws Exception Throw exception when fail to get screen dest content
	 */
	private int getNumberOfSourceOfSuperDest(SuperDestination superDestination) throws Exception {
		int numberOfSource = 0;
		for (int i = 0; i < superDestination.getListScreenDestName().size(); i++) {
			SourceProperties sourceProperties2 = new SourceProperties();
			String currentName = superDestination.getListScreenDestName().get(i);
			int currentDestId = getCurrentScreenDestId(currentName);
			JsonNode currentScreenDestContent = getScreenDestContent(currentDestId);
			updateSourcePropertiesValue(true, currentScreenDestContent, sourceProperties2);
			numberOfSource += sourceProperties2.numberOfSource;
		}
		return numberOfSource;
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

