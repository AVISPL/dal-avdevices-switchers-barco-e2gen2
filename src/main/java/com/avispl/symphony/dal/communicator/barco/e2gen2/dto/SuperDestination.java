/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.SuperDestinationDeserializer;

/**
 * Super Aux Destination DTO class
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
@JsonDeserialize(using = SuperDestinationDeserializer.class)
public class SuperDestination {

	private int id;

	private String name;

	private int hdiMention;

	private int vdiMention;

	private int hSize;

	private int vSize;

	private List<String> listScreenDestName;

	private Map<Integer, String> globalLayerMap;

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
	 * @param id the {@code int} field
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
	 * Retrieves {@code {@link #hdiMention}}
	 *
	 * @return value of {@link #hdiMention}
	 */
	public int getHdiMention() {
		return hdiMention;
	}

	/**
	 * Sets {@code hdiMention}
	 *
	 * @param hdiMention the {@code int} field
	 */
	public void setHdiMention(int hdiMention) {
		this.hdiMention = hdiMention;
	}

	/**
	 * Retrieves {@code {@link #vdiMention}}
	 *
	 * @return value of {@link #vdiMention}
	 */
	public int getVdiMention() {
		return vdiMention;
	}

	/**
	 * Sets {@code vdiMention}
	 *
	 * @param vdiMention the {@code int} field
	 */
	public void setVdiMention(int vdiMention) {
		this.vdiMention = vdiMention;
	}

	/**
	 * Retrieves {@code {@link #hSize}}
	 *
	 * @return value of {@link #hSize}
	 */
	public int gethSize() {
		return hSize;
	}

	/**
	 * Sets {@code hSize}
	 *
	 * @param hSize the {@code int} field
	 */
	public void sethSize(int hSize) {
		this.hSize = hSize;
	}

	/**
	 * Retrieves {@code {@link #vSize}}
	 *
	 * @return value of {@link #vSize}
	 */
	public int getvSize() {
		return vSize;
	}

	/**
	 * Sets {@code vSize}
	 *
	 * @param vSize the {@code int} field
	 */
	public void setvSize(int vSize) {
		this.vSize = vSize;
	}

	/**
	 * Retrieves {@code {@link #globalLayerMap}}
	 *
	 * @return value of {@link #globalLayerMap}
	 */
	public Map<Integer, String> getGlobalLayerMap() {
		return globalLayerMap;
	}

	/**
	 * Sets {@code globalLayerMap}
	 *
	 * @param globalLayerMap the {@code java.util.Map<java.lang.Integer,java.lang.String>} field
	 */
	public void setGlobalLayerMap(Map<Integer, String> globalLayerMap) {
		this.globalLayerMap = globalLayerMap;
	}

	/**
	 * Retrieves {@code {@link #listScreenDestName}}
	 *
	 * @return value of {@link #listScreenDestName}
	 */
	public List<String> getListScreenDestName() {
		return listScreenDestName;
	}

	/**
	 * Sets {@code listScreenDestName}
	 *
	 * @param listScreenDestName the {@code java.util.List<java.lang.String>} field
	 */
	public void setListScreenDestName(List<String> listScreenDestName) {
		this.listScreenDestName = listScreenDestName;
	}
}
