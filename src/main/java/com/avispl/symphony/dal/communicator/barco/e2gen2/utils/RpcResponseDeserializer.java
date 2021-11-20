/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.RpcResponse;

/**
 * Custom Deserializer class for Rpc Response
 *
 * @author Ivan
 * @since 1.0.0
 */
public class RpcResponseDeserializer extends StdDeserializer<RpcResponse> {

	/**
	 * DeviceInfoDeserializer constructor
	 */
	public RpcResponseDeserializer() {
		this(null);
	}

	/**
	 * DeviceInfoDeserializer with arg constructor
	 * @param vc Class
	 */
	protected RpcResponseDeserializer(Class<?> vc) {
		super(vc);
	}

	/**
	 * Override deserialize class for RpcResponse
	 * {@inheritDoc}
	 *
	 * @param jp JsonParser
	 * @param deserializationContext DeserializationContext
	 * @return RpcResponse
	 * @throws IOException Throw exception when failed to get JsonNode
	 */
	@Override
	public RpcResponse deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
		JsonNode rpcResponseNode = jp.getCodec().readTree(jp);
		RpcResponse rpcResponse = new RpcResponse();
		rpcResponse.setSuccessCode(rpcResponseNode.get(BarcoE2Constant.RESULT).get(BarcoE2Constant.SUCCESS).asInt());
		rpcResponse.setResponse(rpcResponseNode.get(BarcoE2Constant.RESULT).get(BarcoE2Constant.RESPONSE));
		return rpcResponse;
	}
}
