/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

/**
 * Metric for Barco E2 Monitoring Properties
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public enum BarcoE2MonitoringMetric {

	FIRMWARE_VERSION("DeviceInformation#FirmwareVersion"),
	IP_ADDRESS("DeviceInformation#IPAddress"),
	DEVICE_NAME("DeviceInformation#DeviceName"),
	DEVICE_ID("DeviceInformation#DeviceID"),
	DEVICE_MODEL("DeviceInformation#DeviceModel"),
	MAC_ADDRESS("DeviceInformation#MACAddress"),
	HOST_NAME("DeviceInformation#HostName"),
	CONNECTED_UNITS("DeviceInformation#ConnectedUnits"),
	POWER_SUPPLY_1_STATUS("PowerStatus#PowerSupply1Status"),
	POWER_SUPPLY_2_STATUS("PowerStatus#PowerSupply2Status");
	private final String name;

	/**
	 * BarcoE2MonitoringMetric constructor
	 *
	 * @param name {@code {@link #name}}
	 */
	BarcoE2MonitoringMetric(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@code {@link #name}}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}
}
