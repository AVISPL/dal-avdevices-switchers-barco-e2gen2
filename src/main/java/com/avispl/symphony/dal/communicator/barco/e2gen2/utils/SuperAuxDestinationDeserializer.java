/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.AuxDestination;
import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.SuperAuxDestination;

/**
 * Custom Deserializer class for SuperAuxDestination
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public class SuperAuxDestinationDeserializer extends StdDeserializer<SuperAuxDestination> {

	/**
	 * SuperAuxDestinationDeserializer constructor
	 */
	public SuperAuxDestinationDeserializer() {
		this(null);
	}

	/**
	 * SuperAuxDestinationDeserializer with arg constructor
	 * @param vc Class
	 */
	protected SuperAuxDestinationDeserializer(Class<?> vc) {
		super(vc);
	}

	/**
	 * Override deserialize class for SuperAuxDestination
	 * {@inheritDoc}
	 *
	 * @param jp JsonParser
	 * @param deserializationContext DeserializationContext
	 * @return RpcResponse
	 * @throws IOException Throw exception when failed to get JsonNode
	 */
	@Override
	public  SuperAuxDestination deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
		JsonNode jsonNode = jp.getCodec().readTree(jp);
		SuperAuxDestination superAuxDestination = new SuperAuxDestination();
		superAuxDestination.setId(jsonNode.get(BarcoE2Constant.ID).asInt());
		superAuxDestination.setName(jsonNode.get(BarcoE2Constant.NAME).asText());
		superAuxDestination.setHdiMention(jsonNode.get(BarcoE2Constant.H_DIMENTION).asInt());
		superAuxDestination.setVdiMention(jsonNode.get(BarcoE2Constant.V_DIMENTION).asInt());
		superAuxDestination.sethSize(jsonNode.get(BarcoE2Constant.H_SIZE).asInt());
		superAuxDestination.sethSize(jsonNode.get(BarcoE2Constant.V_SIZE).asInt());

		JsonNode arrayNode = jsonNode.get(BarcoE2Constant.AUX_DEST_COLLECTION);
		if (arrayNode == null) {
			throw new ResourceNotReachableException("Fail to get collection of aux destination");
		}
		List<AuxDestination> auxDestinationList = new ArrayList<>();
		for (int i = 0; i < arrayNode.size(); i++) {
			if (arrayNode.get(i) == null) {
				continue;
			}
			String name = arrayNode.get(i).get(BarcoE2Constant.NAME).asText();
			int id = arrayNode.get(i).get(BarcoE2Constant.ID).asInt();
			auxDestinationList.add(new AuxDestination(id, name));
		}
		if (auxDestinationList.isEmpty()) {
			throw new ResourceNotReachableException(String.format("Fail to get collection of screen destination in this super destination: %s", superAuxDestination.getName()));
		}
		superAuxDestination.setAuxDestinationList(auxDestinationList);
		return superAuxDestination;
	}
}
