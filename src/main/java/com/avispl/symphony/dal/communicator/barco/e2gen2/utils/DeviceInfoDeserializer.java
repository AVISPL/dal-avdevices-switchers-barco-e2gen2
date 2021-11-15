/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.DeviceInfo;

/**
 * Custom Deserializer class for Device Info
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public class DeviceInfoDeserializer extends StdDeserializer<DeviceInfo> {
	/**
	 * DeviceInfoDeserializer constructor
	 */
	public DeviceInfoDeserializer() {
		this(null);
	}

	protected DeviceInfoDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public DeviceInfo deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
		JsonNode deviceInfoNode = jp.getCodec().readTree(jp);
		DeviceInfo deviceInfo = new DeviceInfo();
		deviceInfo.setDeviceId(deviceInfoNode.get(BarcoE2Constant.ID).asText());
		deviceInfo.setName(deviceInfoNode.get(BarcoE2Constant.NAME).asText());
		deviceInfo.setMacAddress(deviceInfoNode.get(BarcoE2Constant.MAC_ADDRESS).asText());
		deviceInfo.setFrameWareVersion(deviceInfoNode.get(BarcoE2Constant.FRAME_COLLECTION).get(BarcoE2Constant.FRAME).get(BarcoE2Constant.VERSION).asText());
		deviceInfo.setDeviceId(deviceInfoNode.get(BarcoE2Constant.FRAME_COLLECTION).get(BarcoE2Constant.FRAME).get(BarcoE2Constant.ID).asText());
		deviceInfo.setFrameTypeName(deviceInfoNode.get(BarcoE2Constant.FRAME_COLLECTION).get(BarcoE2Constant.FRAME).get(BarcoE2Constant.FRAME_TYPE_NAME).asText());
		deviceInfo.setDhcpMode(deviceInfoNode.get(BarcoE2Constant.FRAME_COLLECTION).get(BarcoE2Constant.FRAME).get(BarcoE2Constant.ENET).get(BarcoE2Constant.DHCP_MODE).asBoolean());
		deviceInfo.setIp(deviceInfoNode.get(BarcoE2Constant.FRAME_COLLECTION).get(BarcoE2Constant.FRAME).get(BarcoE2Constant.ENET).get(BarcoE2Constant.IP).asText());
		deviceInfo.setStaticIP(deviceInfoNode.get(BarcoE2Constant.FRAME_COLLECTION).get(BarcoE2Constant.FRAME).get(BarcoE2Constant.ENET).get(BarcoE2Constant.STATIC_IP).asText());
		return deviceInfo;
	}
}
