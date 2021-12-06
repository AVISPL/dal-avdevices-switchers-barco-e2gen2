/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.HashMap;
import java.util.List;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.dal.communicator.HttpCommunicator;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2ControllingMetric;
import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2MonitoringMetric;

/**
 * Unit test for {@link BarcoE2Communicator}.
 * Covered cases: get device information, get JsonNode with exception, preset control.
 *
 * @author Ivan
 * @since 1.0.0
 */
class BarcoE2CommunicatorTest {

	private static final int HTTP_PORT = 9999;
	private static final String HOST_NAME = "127.0.0.1";
	private static final String PROTOCOL = "http";

	static BarcoE2Communicator barcoE2Communicator;

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(options().port(HTTP_PORT)
			.bindAddress(HOST_NAME));

	@BeforeEach
	public void init() throws Exception {
		wireMockRule.start();
		barcoE2Communicator = new BarcoE2Communicator();
		barcoE2Communicator.setTrustAllCertificates(true);
		barcoE2Communicator.setTimeout(2000);
		barcoE2Communicator.setProtocol(PROTOCOL);
		barcoE2Communicator.setPort(HTTP_PORT);
		barcoE2Communicator.setHost(HOST_NAME);
		barcoE2Communicator.setContentType("application/json");
		barcoE2Communicator.setAuthenticationScheme(HttpCommunicator.AuthenticationScheme.None);
		barcoE2Communicator.setListSuperScreenDestId("0");
		barcoE2Communicator.setListSuperAuxDestId("0,1");
		barcoE2Communicator.init();
	}

	@AfterEach
	public void stopBarcoE2Communicator() {
		barcoE2Communicator.destroy();
		wireMockRule.stop();
	}

	/**
	 * Test method for {@link BarcoE2Communicator#getMultipleStatistics()}, get device information
	 *
	 * @throws Exception Throw exceptions when cannot call request on the device
	 */
	@Test
	void deviceInformationAndPowerSupplyTest() throws Exception {
				List<Statistics> statistics = barcoE2Communicator.getMultipleStatistics();
		Assertions.assertNotNull(statistics.get(0));
		assertEquals(1, statistics.size());
		assertEquals("08:00:27:d9:48:fd", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.MAC_ADDRESS.getName()));
		assertEquals("08:00:27:d9:48:fd", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.DEVICE_ID.getName()));
		assertEquals("2", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.CONNECTED_UNITS.getName()));
		assertEquals("9.0.4878", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.FIRMWARE_VERSION.getName()));
		assertEquals("192.168.000.175", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.IP_ADDRESS.getName()));
		assertEquals("System1", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.HOST_NAME.getName()));
		assertEquals("System1", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.DEVICE_NAME.getName()));
		assertEquals("E2", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.DEVICE_MODEL.getName()));
		assertEquals("OK", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.POWER_SUPPLY_1_STATUS.getName()));
		assertEquals("OK", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.POWER_SUPPLY_2_STATUS.getName()));
	}

	/**
	 * Test method for {@link BarcoE2Communicator#getMultipleStatistics()}, get device information
	 *	None when failed to get device information
	 * @throws Exception Throw exceptions when cannot call request on the device
	 */
	@Test
	void deviceInformationWithNone() throws Exception {
		barcoE2Communicator.destroy();
		barcoE2Communicator.setBaseUri("/null-device-information-power-status");
		barcoE2Communicator.init();
		List<Statistics> statistics = barcoE2Communicator.getMultipleStatistics();
		Assertions.assertNotNull(statistics.get(0));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.MAC_ADDRESS.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.DEVICE_ID.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.CONNECTED_UNITS.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.FIRMWARE_VERSION.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.IP_ADDRESS.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.HOST_NAME.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.DEVICE_NAME.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.DEVICE_MODEL.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.POWER_SUPPLY_1_STATUS.getName()));
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2MonitoringMetric.POWER_SUPPLY_2_STATUS.getName()));
	}

	/**
	 * Test method for Preset Control
	 * 	Preset in the device
	 */
	@Test
	void testForPresetInDevice() throws Exception {
		barcoE2Communicator.destroy();
		barcoE2Communicator.setBaseUri("/");
		barcoE2Communicator.init();
		barcoE2Communicator.getMultipleStatistics();
		List<Statistics> statistics = barcoE2Communicator.getMultipleStatistics();
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2ControllingMetric.PRESETS_PRESET.getName()));
	}

	/**
	 * Test method for Preset Control
	 * No preset in the device
	 */
	@Test
	void testForNoPresetInDevice() throws Exception {
		barcoE2Communicator.destroy();
		barcoE2Communicator.setBaseUri("/expect-exception");
		barcoE2Communicator.init();
		barcoE2Communicator.getMultipleStatistics();
		List<Statistics> statistics = barcoE2Communicator.getMultipleStatistics();
		// No preset in the device => dropdown list = None
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2ControllingMetric.PRESETS_PRESET.getName()));
	}

	/**
	 * Test method for Preset Control
	 * No active preset in the device
	 */
	@Test
	void testForNoActivePreset() throws Exception {
		barcoE2Communicator.destroy();
		barcoE2Communicator.setBaseUri("/expect-exception");
		barcoE2Communicator.init();
		barcoE2Communicator.getMultipleStatistics();
		List<Statistics> statistics = barcoE2Communicator.getMultipleStatistics();
		assertEquals("None", ((ExtendedStatistics) statistics.get(0)).getStatistics().get(BarcoE2ControllingMetric.PRESETS_LAST_CALLED_PRESET.getName()));
	}

	/**
	 * Test method for JsonNode
	 *  Exception when response is null
	 *
	 * @throws Exception
	 */
	@Test
	void testGetJsonNodeWithNullResponse() throws Exception {
		barcoE2Communicator.destroy();
		barcoE2Communicator.setBaseUri("/expect-exception");
		barcoE2Communicator.init();
		assertThrows(ResourceNotReachableException.class, () -> barcoE2Communicator.requestByMethod("nullResponseMethod", new HashMap<>()), "Expect exception here due to null response");
	}

	/**
	 * Test method for JsonNode
	 *  Exception when response is null
	 *
	 * @throws Exception
	 */
	@Test
	void testGetJsonNodeWithNotExistedMethod() throws Exception {
		barcoE2Communicator.destroy();
		barcoE2Communicator.setBaseUri("/expect-exception");
		barcoE2Communicator.init();
		assertThrows(Exception.class, () -> barcoE2Communicator.requestByMethod("notExistedMethod", new HashMap<>()), "Expect exception doPost on not existed method");
	}
}
