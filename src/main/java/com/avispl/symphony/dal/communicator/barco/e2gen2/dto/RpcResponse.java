/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.avispl.symphony.dal.communicator.barco.e2gen2.utils.RpcResponseDeserializer;

/**
 * Rpc Response DTO
 *
 * @author Ivan
 * @since 1.0.0
 */
@JsonDeserialize(using = RpcResponseDeserializer.class)
public class RpcResponse {
	private String jsonrpc = "jsonrpc";
	private Integer successCode;
	private JsonNode response;
	private int id = 1234;

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
	 * Retrieves {@code {@link #successCode}}
	 *
	 * @return value of {@link #successCode}
	 */
	public Integer getSuccessCode() {
		return successCode;
	}

	/**
	 * Sets {@code successCode}
	 *
	 * @param successCode the {@code java.lang.Integer} field
	 */
	public void setSuccessCode(Integer successCode) {
		this.successCode = successCode;
	}

	/**
	 * Retrieves {@code {@link #response}}
	 *
	 * @return value of {@link #response}
	 */
	public JsonNode getResponse() {
		return response;
	}

	/**
	 * Sets {@code response}
	 *
	 * @param response the {@code com.fasterxml.jackson.databind.JsonNode} field
	 */
	public void setResponse(JsonNode response) {
		this.response = response;
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
