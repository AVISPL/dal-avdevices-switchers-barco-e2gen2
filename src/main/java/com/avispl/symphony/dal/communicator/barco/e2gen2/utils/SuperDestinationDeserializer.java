/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.avispl.symphony.dal.communicator.barco.e2gen2.dto.SuperDestination;

/**
 * Custom Deserializer class for SuperAuxDestination
 *
 * @author Duy Nguyen
 * @since 1.0.0
 */
public class SuperDestinationDeserializer extends StdDeserializer<SuperDestination> {

	/**
	 * SuperAuxDestinationDeserializer constructor
	 */
	public SuperDestinationDeserializer() {
		this(null);
	}

	/**
	 * SuperAuxDestinationDeserializer with arg constructor
	 *
	 * @param vc Class
	 */
	protected SuperDestinationDeserializer(Class<?> vc) {
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
	public SuperDestination deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {

		JsonNode jsonNode = jp.getCodec().readTree(jp);
		SuperDestination superDestination = new SuperDestination();
		superDestination.setId(jsonNode.get(BarcoE2Constant.ID).asInt());
		superDestination.setName(jsonNode.get(BarcoE2Constant.NAME).asText());
		superDestination.setHdiMention(jsonNode.get(BarcoE2Constant.H_DIMENTION).asInt());
		superDestination.setVdiMention(jsonNode.get(BarcoE2Constant.V_DIMENTION).asInt());
		superDestination.sethSize(jsonNode.get(BarcoE2Constant.H_SIZE).asInt());
		superDestination.sethSize(jsonNode.get(BarcoE2Constant.V_SIZE).asInt());
		List<String> listDestName = new ArrayList<>();
		JsonNode destCollectionNode = jsonNode.get(BarcoE2Constant.DEST_COLLECTION);

		if (destCollectionNode.get(0) == null) {
			listDestName = Collections.emptyList();
		} else {
			for (int i = 0; i < destCollectionNode.size(); i++) {
				String destName = destCollectionNode.get(i).get(BarcoE2Constant.NAME).asText();
				listDestName.add(destName);
			}
		}
		superDestination.setListScreenDestName(listDestName);

		Map<Integer, String> globalLayerMap = new HashMap<>();
		if (jsonNode.get(BarcoE2Constant.GLOBAL_LAYERS).asInt() != 0) {
			JsonNode globalLayerNode = jsonNode.get(BarcoE2Constant.GLOBAL_LAYER_COLLECTION).get(BarcoE2Constant.GLOBAL_LAYER);
			if (globalLayerNode != null && globalLayerNode.size() != 0 && !globalLayerNode.isEmpty()) {
				for (int i = 0; i < globalLayerNode.size(); i++) {
					String globalLayerName = globalLayerNode.get(i).get(BarcoE2Constant.NAME).asText();
					int globalLayerId = globalLayerNode.get(i).get(BarcoE2Constant.ID).asInt();
					globalLayerMap.put(globalLayerId, globalLayerName);
				}
			}
		}
		superDestination.setGlobalLayerMap(globalLayerMap);
		return superDestination;
	}
}
