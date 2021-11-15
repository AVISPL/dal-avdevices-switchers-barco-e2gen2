/*
 * Copyright (c) 2021 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.barco.e2gen2.utils;

/**
 * Metric for Barco E2 Monitoring Properties
 *
 * @author Ivan
 * @since 1.0.0
 */
public enum BarcoE2ControllingMetric {
	PRESETS_LAST_CALLED_PRESET("Presets#LastCalledPreset"), PRESETS_PRESET("Presets#Preset"),
	PRESETS_PRESET_ACTIVATE("Presets#PresetActivate");
	private final String name;

	BarcoE2ControllingMetric(String name) {
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

	public static BarcoE2ControllingMetric getByName(String name) {
		for (BarcoE2ControllingMetric metric: BarcoE2ControllingMetric.values()
		) {
			if (metric.getName().equals(name)) {
				return metric;
			}
		}
		throw new IllegalArgumentException("Cannot find the enum with name: " + name);
	}
}
