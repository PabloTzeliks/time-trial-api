package com.centroweg.iot.time_trial_api.inbound.dto;

public record SensorPayloadDTO(String rfid, Long timestampMs) { }
