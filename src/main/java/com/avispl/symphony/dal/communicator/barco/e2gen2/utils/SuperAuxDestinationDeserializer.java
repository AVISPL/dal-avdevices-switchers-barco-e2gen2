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
	 * @param vc
	 */
	protected SuperAuxDestinationDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public  SuperAuxDestination deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
		JsonNode jsonNode = jp.getCodec().readTree(jp);
		SuperAuxDestination superAuxDestination = new SuperAuxDestination();
		superAuxDestination.setId(jsonNode.get(BarcoE2Constant.ID).asInt());
		superAuxDestination.setName(jsonNode.get(BarcoE2Constant.NAME).asText());
		superAuxDestination.setHdiMention(jsonNode.get("HDimention").asInt());
		superAuxDestination.setVdiMention(jsonNode.get("VDimention").asInt());
		superAuxDestination.sethSize(jsonNode.get("HSize").asInt());
		superAuxDestination.sethSize(jsonNode.get("VSize").asInt());
		JsonNode arrayNode = jsonNode.get("AuxDestCollection");
		List<AuxDestination> auxDestinationList = new ArrayList<>();
		for (int i = 0; i < arrayNode.size(); i++) {
			String name = arrayNode.get(i).get(BarcoE2Constant.NAME).asText();
			int id = arrayNode.get(i).get(BarcoE2Constant.ID).asInt();
			auxDestinationList.add(new AuxDestination(id, name));
		}
		superAuxDestination.setAuxDestinationList(auxDestinationList);
		return superAuxDestination;
	}
}
