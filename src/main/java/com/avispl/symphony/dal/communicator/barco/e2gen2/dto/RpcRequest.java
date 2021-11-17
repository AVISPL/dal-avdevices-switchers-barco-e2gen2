/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.BarcoE2Constant;

/**
 * Rpc Request DTO
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public class RpcRequest {
	@JsonProperty("jsonrpc")
	private String jsonrpc = BarcoE2Constant.DEFAULT_RPC_VERSION;
	@JsonProperty("method")
	private String method;
	@JsonProperty("params")
	private Map<Object, Object> params;
	@JsonProperty("id")
	private int id = BarcoE2Constant.DEFAULT_ID;

	/**
	 * Retrieves {@code {@link #jsonrpc}}
	 *
	 * @return value of {@link #jsonrpc}
	 */
	public String getJsonrpc() {
		return jsonrpc;
	}

	/**
	 * Sets {@code jsonrpc}
	 *
	 * @param jsonrpc the {@code java.lang.String} field
	 */
	public void setJsonrpc(String jsonrpc) {
		this.jsonrpc = jsonrpc;
	}

	/**
	 * Retrieves {@code {@link #method}}
	 *
	 * @return value of {@link #method}
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Sets {@code method}
	 *
	 * @param method the {@code java.lang.String} field
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Retrieves {@code {@link #params}}
	 *
	 * @return value of {@link #params}
	 */
	public Map<Object, Object> getParams() {
		return params;
	}

	/**
	 * Sets {@code params}
	 *
	 * @param params the {@code java.util.Map<java.lang.String,java.lang.String>} field
	 */
	public void setParams(Map<Object, Object> params) {
		this.params = params;
	}

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
}
