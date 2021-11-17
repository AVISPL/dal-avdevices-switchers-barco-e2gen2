/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.DeviceInfo;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.PowerStatusDTO;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.RpcRequest;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.RpcResponse;
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

	private ExtendedStatistics localExtendedStatistics;
	private String lastPresetName = BarcoE2Constant.DOUBLE_QUOTES;
	private String lastScreenDestName = BarcoE2Constant.DOUBLE_QUOTES;
	private boolean isFirstTimeMonitor = true;

	/**
	 * This method is called by Symphony to get the list of statistics to be displayed
	 * {@inheritDoc}
	 *
	 * @return List<Statistics> This returns the list of statistics
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Perform doPost() at host %s with port %s", this.host, this.getPort()));
		}
		Map<String, String> statistics = new HashMap<>();
		List<AdvancedControllableProperty> controls = new ArrayList<>();
		if (localExtendedStatistics == null) {
			localExtendedStatistics = new ExtendedStatistics();
		} else {
			statistics = localExtendedStatistics.getStatistics();
		}
		initializeData(statistics, controls);
		localExtendedStatistics.setStatistics(statistics);
		localExtendedStatistics.setControllableProperties(controls);
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * Initialize monitoring/controlling data
	 *
	 * @param statistics Map of statistics
	 * @param controls List of AdvancedControllableProperty that need to be controlled
	 */
	private void initializeData(Map<String, String> statistics, List<AdvancedControllableProperty> controls) throws Exception {
		if (isFirstTimeMonitor) {
			getDeviceInformation(statistics);
			isFirstTimeMonitor = false;
		}
		getPresetFeedBack(statistics, controls);
		// TODO: changeContent API not working
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
	 * Call post request
	 *
	 * @return JsonNode returns the JsonNode of given object.
	 */
	protected JsonNode getJsonNode(String method, Map<Object, Object> param) throws Exception {
		RpcResponse rpcResponse;
		try {
			rpcResponse = this.doPost(BarcoE2Constant.DOUBLE_QUOTES, rpcRequestBody(method, param), RpcResponse.class);
			if (rpcResponse != null && rpcResponse.getSuccessCode() == 0) {
				if (!NullNode.instance.equals(rpcResponse.getResponse()) && rpcResponse.getResponse() != null) {
					return rpcResponse.getResponse();
				} else {
					return new ObjectMapper().valueToTree(rpcResponse);
				}
			} else {
				throw new ResourceNotReachableException("doPost success but failed to get data from Json RPC");
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn(String.format("Failed to doPost for method %s", method));
			}
			throw e;
		}
	}

	/**
	 * Map a JsonNode to DTO
	 *
	 * @param jsonNode input jsonNode that need to be converted
	 * @return This returns the DTO of given Object.
	 */
	private <T> Object jsonNodeToDTO(JsonNode jsonNode, Class<T> tClass) throws JsonProcessingException {
		ObjectMapper jsonObjectMapper = new ObjectMapper();
		try {
			return jsonObjectMapper.treeToValue(jsonNode, tClass);
		} catch (JsonProcessingException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to convert jsonNode to DTO");
			}
			throw e;
		}
	}

	/**
	 * BarcoE2Communicator doesn't require authentication
	 *
	 * {@inheritDoc}
	 */
	@Override
	protected void authenticate() {
		// BarcoE2Communicator doesn't require authentication
	}

	/**
	 * Properties that need to be controlled:
	 * - Routing Control
	 * - Preset Control
	 *
	 * {@inheritDoc}
	 *
	 * @throws Exception Throw exception if failed to activePreset, get json node, changeAuxContent
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
				boolean result;
				result = activatePresetResult();
				if (logger.isDebugEnabled()) {
					String debugString = result ? String.format("Activate %s success!", lastPresetName) : String.format("Activate %s fail!", lastPresetName);
					logger.debug(debugString);
				}
				break;
			case SCREEN_DESTINATION:

				break;
			case AUX_DESTINATION:

				break;
			default:
				if (logger.isWarnEnabled()) {
					logger.warn(String.format("Operation %s with value %s is not supported.", property, value));
				}
				throw new IllegalArgumentException(String.format("Operation %s with value %s is not supported.", property, value));
		}
	}

	/**
	 * Control properties
	 *
	 * {@inheritDoc}
	 *
	 * @return List<ControllableProperty> This returns the list of ControllableProperty
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
	 * Monitor: Retrieve device information.
	 *
	 * @param stats Map of statistics
	 */
	private void getDeviceInformation(Map<String, String> stats) throws Exception {
		JsonNode deviceInfoResponse = getJsonNode(BarcoE2Constant.METHOD_GET_FRAME_SETTINGS, new HashMap<>()).get(BarcoE2Constant.SYSTEM);
		DeviceInfo deviceInfo = (DeviceInfo) jsonNodeToDTO(deviceInfoResponse, DeviceInfo.class);
		String macAddress = deviceInfo.getMacAddress();
		JsonNode powerStatusResponse = getJsonNode(BarcoE2Constant.METHOD_POWER_STATUS, new HashMap<>());
		deviceInfo.setConnectedUnit(powerStatusResponse.size());
		PowerStatusDTO powerStatusDTO = (PowerStatusDTO) jsonNodeToDTO(powerStatusResponse.get(macAddress), PowerStatusDTO.class);
		// put monitoring data to stats
		populateDeviceInformationData(stats, deviceInfo, powerStatusDTO);
	}

	/**
	 * Populate data for device information
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
		stats.put(BarcoE2MonitoringMetric.DEVICE_NAME.getName(), deviceInfo.getFrameTypeName());
		stats.put(BarcoE2MonitoringMetric.DEVICE_ID.getName(), deviceInfo.getDeviceId());
		stats.put(BarcoE2MonitoringMetric.DEVICE_MODEL.getName(), deviceInfo.getFrameTypeName());
		stats.put(BarcoE2MonitoringMetric.MAC_ADDRESS.getName(), deviceInfo.getMacAddress());
		stats.put(BarcoE2MonitoringMetric.HOST_NAME.getName(), deviceInfo.getFrameTypeName());
	}

	/**
	 * Convert int power status to String
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
		String activePresetResult = getActivePresetName();
		stats.put(BarcoE2ControllingMetric.PRESETS_LAST_CALLED_PRESET.getName(), activePresetResult);
		// Generate dropdown options
		List<String> dropDownOptions = generateDropDownOptions();
		// Set lastPresetName to first index of dropdown, else lastPresetName = activePresetResult
		if (activePresetResult.equals(BarcoE2Constant.NONE) && !dropDownOptions.isEmpty()) {
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
		JsonNode response = getJsonNode(BarcoE2Constant.METHOD_LIST_DESTINATIONS_FOR_PRESET, presetParam);
		List<String> dropDownOptions = new ArrayList<>();
		if (response.size() == 0) {
			return Collections.emptyList();
		}
		for (int i = 0; i < response.size(); i++) {
			String presetName = String.valueOf(response.get(i).get(BarcoE2Constant.NAME).asText());
			dropDownOptions.add(presetName);
		}
		return dropDownOptions;
	}

	/**
	 * Preset Control: Get current activate preset name
	 *
	 * @return This returns a preset name
	 * @throws Exception Throw exception if failed to get json node
	 */
	private String getActivePresetName() throws Exception {
		JsonNode recallPresetResponse = getJsonNode(BarcoE2Constant.METHOD_LAST_RECALLED_PRESET, new HashMap<>());
		if (recallPresetResponse != null) {
			JsonNode response;
			if (recallPresetResponse.isNumber()) {
				response = recallPresetResponse;
			} else {
				response = recallPresetResponse.get(BarcoE2Constant.RESPONSE);
			}
			String activePresetResult;
			if (response.isNull()) {
				activePresetResult = BarcoE2Constant.NONE;
			} else {
				int activePresetIndex = response.asInt();
				Map<Object, Object> presetParam = new HashMap<>();
				presetParam.put(BarcoE2Constant.ID, activePresetIndex);
				JsonNode presetResponse = getJsonNode(BarcoE2Constant.METHOD_LIST_DESTINATIONS_FOR_PRESET, presetParam);
				activePresetResult = String.valueOf(presetResponse.get(BarcoE2Constant.NAME).asText());
			}
			return activePresetResult;
		}
		throw new ResourceNotReachableException("Cannot get last called preset");
	}

	/**
	 * Preset Control: Populate data for preset
	 *
	 * @param stats Map of statistics
	 * @param controls List of AdvancedControllableProperty
	 * @param dropDownOptions List of dropdown options
	 */
	private void populatePresetStatsAndControls(Map<String, String> stats, List<AdvancedControllableProperty> controls, List<String> dropDownOptions, String activePresetResult) {
		if (dropDownOptions.isEmpty()) {
			stats.put(BarcoE2ControllingMetric.PRESETS_PRESET.getName(), BarcoE2Constant.NONE);
		} else {
			stats.put(BarcoE2ControllingMetric.PRESETS_PRESET.getName(), BarcoE2Constant.DOUBLE_QUOTES);
			controls.add(createDropdown(BarcoE2ControllingMetric.PRESETS_PRESET.getName(), activePresetResult, dropDownOptions));
		}
		stats.put(BarcoE2ControllingMetric.PRESETS_PRESET_ACTIVATE.getName(), BarcoE2Constant.DOUBLE_QUOTES);
		controls.add(createButton(BarcoE2ControllingMetric.PRESETS_PRESET_ACTIVATE.getName(),
				BarcoE2Constant.LABEL_ACTIVATE_ON_PROGRAM, BarcoE2Constant.LABEL_PRESSED_RECALLING_PRESET, BarcoE2Constant.GRACE_PERIOD));
	}

	/**
	 * Preset Control: Activate the preset
	 *
	 * @return Boolean this returns true/false based on the result of activating preset
	 * @throws Exception Throws exception if failed to activate
	 */
	private boolean activatePresetResult() throws Exception {
		boolean result;
		if (!Objects.equals(lastPresetName, BarcoE2Constant.DOUBLE_QUOTES)) {
			result = activatePreset(lastPresetName);
		} else {
			// first time running
			String activePresetResult = getActivePresetName();
			if (activePresetResult.equals(BarcoE2Constant.NONE)) {
				result = activatePreset(lastPresetName);
			} else {
				result = activatePreset(activePresetResult);
			}
		}
		return result;
	}
	/**
	 * Preset Control: Activate a preset by name and type
	 *
	 * @param presetName name of the preset that need to be activated
	 * @return boolean type indicates that preset is successfully activated.
	 */
	private boolean activatePreset(String presetName) throws Exception {
		Map<Object, Object> param = new HashMap<>();
		param.put(BarcoE2Constant.PRESET_NAME, presetName);
		param.put(BarcoE2Constant.TYPE, 1);
		return getJsonNode(BarcoE2Constant.METHOD_ACTIVATE_PRESET, param).get(BarcoE2Constant.ACTIVATE_PRESET_SUCCESS).asInt() == 0;
	}

	/**
	 * Create a button.
	 *
	 * @param name name of the button
	 * @param label label of the button
	 * @param labelPressed label of the button after pressing it
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
