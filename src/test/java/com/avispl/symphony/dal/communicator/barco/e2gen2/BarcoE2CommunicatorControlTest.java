/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.dal.communicator.HttpCommunicator;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2Constant;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2ControllingMetric;

@ExtendWith(MockitoExtension.class)
class BarcoE2CommunicatorControlTest {

	private static final int HTTP_PORT = 9999;
	private static final String HOST_NAME = "127.0.0.1";
	private static final String PROTOCOL = "http";

	@Spy
	@InjectMocks
	private BarcoE2Communicator barcoE2Communicator;

	@RegisterExtension
	static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
			.options(wireMockConfig()
					.port(HTTP_PORT)
					.bindAddress(HOST_NAME)).build();

	@BeforeEach
	public void init() throws Exception {
		barcoE2Communicator.setTrustAllCertificates(true);
		barcoE2Communicator.setTimeout(10000);
		barcoE2Communicator.setProtocol(PROTOCOL);
		barcoE2Communicator.setPort(HTTP_PORT);
		barcoE2Communicator.setHost(HOST_NAME);
		barcoE2Communicator.setContentType("application/json");
		barcoE2Communicator.setAuthenticationScheme(HttpCommunicator.AuthenticationScheme.None);
		barcoE2Communicator.setListSuperScreenDestId("0");
		barcoE2Communicator.setListSuperAuxDestId("0");
		barcoE2Communicator.init();
	}

	@AfterEach
	public void destroy() {
		barcoE2Communicator.destroy();
	}

	/**
	 * Test method for Preset Control
	 * Case 1: Using the default value( Last Recalled Preset value) from the dropdown list, then clicked on "Activate on Program"
	 *
	 * @throws Exception Throw exception if failed to retrieve statistics or controls
	 */
	@Test
	void testPresetControlCaseDefaultValue() throws Exception {
		barcoE2Communicator.getMultipleStatistics();
		ControllableProperty property = new ControllableProperty();
		// Press the "Activate On Program" button.
		property.setValue(1);
		property.setProperty(BarcoE2ControllingMetric.PRESETS_PRESET_ACTIVATE.getName());
		barcoE2Communicator.controlProperty(property);
		Map<Object, Object> param = new HashMap<>();
		param.put(BarcoE2Constant.PRESET_NAME, "Screen+Super");
		param.put(BarcoE2Constant.TYPE, 1);
		Mockito.verify(barcoE2Communicator,times(1)).requestByMethod(BarcoE2Constant.METHOD_ACTIVATE_PRESET, param);
	}

	/**
	 * Test method for Preset Control
	 * Case 2: Change the value from the dropdown list, then clicked on "Activate on Program"
	 *
	 * @throws Exception Throw exception if failed to retrieve statistics or controls
	 */
	@Test
	void testPresetControlCaseDropdownList() throws Exception {
		barcoE2Communicator.getMultipleStatistics();
		ControllableProperty property = new ControllableProperty();
		// Property of dropdown list
		property.setProperty(BarcoE2ControllingMetric.PRESETS_PRESET.getName());
		property.setValue("Screen+Super");
		barcoE2Communicator.controlProperty(property);
		// Press the "Activate On Program" button.
		property.setValue(1);
		property.setProperty(BarcoE2ControllingMetric.PRESETS_PRESET_ACTIVATE.getName());
		barcoE2Communicator.controlProperty(property);
		Map<Object, Object> param = new HashMap<>();
		param.put(BarcoE2Constant.PRESET_NAME, "Screen+Super");
		param.put(BarcoE2Constant.TYPE, 1);
		Mockito.verify(barcoE2Communicator,times(1)).requestByMethod(BarcoE2Constant.METHOD_ACTIVATE_PRESET, param);
	}

	/**
	 * Test method for Routing Control
	 * 	Test assign source to aux destination
	 *
	 * @throws Exception Throw exception if failed to retrieve statistics or controls
	 */
	@Test
	void testRoutingControlCaseAuxDest() throws Exception {
		barcoE2Communicator.getMultipleStatistics();
		ControllableProperty property = new ControllableProperty();
		// Press the "Activate On Program" button.
		property.setValue("CAM1-1");
		property.setProperty(String.format("%s#%s", BarcoE2ControllingMetric.AUX_DESTINATION.getName(),"DSM"));
		barcoE2Communicator.controlProperty(property);
		Map<Object, Object> params = new HashMap<>();
		params.put(BarcoE2Constant.ID, 0);
		params.put(BarcoE2Constant.PGM_LAST_SRC_INDEX, 0);
		Mockito.verify(barcoE2Communicator,times(1)).requestByMethod(BarcoE2Constant.METHOD_CHANGE_AUX_CONTENT, params);
	}

	/**
	 * Test method for Routing Control
	 * 	Test assign source to super aux destination
	 *
	 * @throws Exception Throw exception if failed to retrieve statistics or controls
	 */
	@Test
	void testRoutingControlCaseSuperAuxDest() throws Exception {
		barcoE2Communicator.getMultipleStatistics();
		ControllableProperty property = new ControllableProperty();
		// Press the "Activate On Program" button.
		property.setValue("CAM2-2");
		property.setProperty(String.format("%s#%s-%s", BarcoE2ControllingMetric.SUPER_AUX_DESTINATION.getName(),"SuperAux1","DSM"));
		barcoE2Communicator.controlProperty(property);
		Map<Object, Object> params = new HashMap<>();
		params.put(BarcoE2Constant.ID, 0);
		params.put(BarcoE2Constant.PGM_LAST_SRC_INDEX, 1);
		Mockito.verify(barcoE2Communicator,times(1)).requestByMethod(BarcoE2Constant.METHOD_CHANGE_AUX_CONTENT, params);
	}

	/**
	 * Test method for Routing Control
	 * 	Test assign source to aux destination
	 *
	 * @throws Exception Throw exception if failed to retrieve statistics or controls
	 */
	@Test
	void testRoutingControlCaseScreenDest() throws Exception {
		barcoE2Communicator.getMultipleStatistics();
		ControllableProperty property = new ControllableProperty();
		// Press the "Activate On Program" button.
		property.setValue("CAM1-1");
		property.setProperty(String.format("%s#%s", BarcoE2ControllingMetric.SCREEN_DESTINATION.getName(),"Main Screen"));
		barcoE2Communicator.controlProperty(property);
		Mockito.verify(barcoE2Communicator,times(1)).controlProperty(property);
	}

	/**
	 * Test method for Routing Control
	 * 	Test assign source to aux destination
	 *
	 * @throws Exception Throw exception if failed to retrieve statistics or controls
	 */
	@Test
	void testRoutingControlCaseSuperScreenDest() throws Exception {
		ControllableProperty property = new ControllableProperty();
		// Press the "Activate On Program" button.
		property.setValue("CAM1-1");
		property.setProperty(String.format("%s#%s", BarcoE2ControllingMetric.SUPER_SCREEN_DESTINATION.getName(),"SuperDest1"));
		barcoE2Communicator.controlProperty(property);
		Mockito.verify(barcoE2Communicator,times(1)).controlProperty(property);
	}
}
