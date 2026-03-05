package com.centroweg.iot.time_trial_api.core.event;

public record CarroPassouNoSensorEvent(
        String rfid,
        Long timestampMs
) { }
