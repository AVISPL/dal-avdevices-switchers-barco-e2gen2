/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.DeviceInfoDeserializer;

/**
 * Device Information DTO.
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
@JsonDeserialize(using = DeviceInfoDeserializer.class)
public class DeviceInfo {

	private int id;

	private String name;

	private String macAddress;

	private String frameWareVersion;

	private String deviceId;

	private String frameTypeName;

	private boolean dhcpMode;

	private String ip;

	private String staticIP;

	@JsonIgnore
	private int connectedUnit;

	/**
	 * Retrieves {@code {@link #id}}
	 *
	 * @return value of {@link #id}
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets {@code id}
	 *
	 * @param id the {@code java.lang.String} field
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Retrieves {@code {@link #name}}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@code name}
	 *
	 * @param name the {@code java.lang.String} field
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@code {@link #macAddress}}
	 *
	 * @return value of {@link #macAddress}
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * Sets {@code macAddress}
	 *
	 * @param macAddress the {@code java.lang.String} field
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * Retrieves {@code {@link #frameWareVersion}}
	 *
	 * @return value of {@link #frameWareVersion}
	 */
	public String getFrameWareVersion() {
		return frameWareVersion;
	}

	/**
	 * Sets {@code frameWareVersion}
	 *
	 * @param frameWareVersion the {@code java.lang.String} field
	 */
	public void setFrameWareVersion(String frameWareVersion) {
		this.frameWareVersion = frameWareVersion;
	}

	/**
	 * Retrieves {@code {@link #deviceId}}
	 *
	 * @return value of {@link #deviceId}
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Sets {@code deviceId}
	 *
	 * @param deviceId the {@code java.lang.String} field
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * Retrieves {@code {@link #frameTypeName}}
	 *
	 * @return value of {@link #frameTypeName}
	 */
	public String getFrameTypeName() {
		return frameTypeName;
	}

	/**
	 * Sets {@code frameTypeName}
	 *
	 * @param frameTypeName the {@code java.lang.String} field
	 */
	public void setFrameTypeName(String frameTypeName) {
		this.frameTypeName = frameTypeName;
	}

	/**
	 * Retrieves {@code {@link #dhcpMode}}
	 *
	 * @return value of {@link #dhcpMode}
	 */
	public boolean isDhcpMode() {
		return dhcpMode;
	}

	/**
	 * Sets {@code dhcpMode}
	 *
	 * @param dhcpMode the {@code boolean} field
	 */
	public void setDhcpMode(boolean dhcpMode) {
		this.dhcpMode = dhcpMode;
	}

	/**
	 * Retrieves {@code {@link #ip}}
	 *
	 * @return value of {@link #ip}
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Sets {@code ip}
	 *
	 * @param ip the {@code java.lang.String} field
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Retrieves {@code {@link #staticIP}}
	 *
	 * @return value of {@link #staticIP}
	 */
	public String getStaticIP() {
		return staticIP;
	}

	/**
	 * Retrieves {@code {@link #connectedUnit}}
	 *
	 * @return value of {@link #connectedUnit}
	 */
	public int getConnectedUnit() {
		return connectedUnit;
	}

	/**
	 * Sets {@code connectedUnit}
	 *
	 * @param connectedUnit the {@code int} field
	 */
	public void setConnectedUnit(int connectedUnit) {
		this.connectedUnit = connectedUnit;
	}

	/**
	 * Sets {@code staticIP}
	 *
	 * @param staticIP the {@code java.lang.String} field
	 */
	public void setStaticIP(String staticIP) {
		this.staticIP = staticIP;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (this == o) {
			return true;
		}
		DeviceInfo that = (DeviceInfo) o;
		return dhcpMode == that.dhcpMode && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(macAddress, that.macAddress)
				&& Objects.equals(frameWareVersion, that.frameWareVersion) && Objects.equals(deviceId, that.deviceId) && Objects.equals(frameTypeName, that.frameTypeName)
				&& Objects.equals(ip, that.ip) && Objects.equals(staticIP, that.staticIP);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, macAddress, frameWareVersion, deviceId, frameTypeName, dhcpMode, ip, staticIP);
	}
}
