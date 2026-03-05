package com.centroweg.iot.time_trial_api.core.event;

public record VoltaValidaCalculadaEvent(
        String rfid,
        Long tempoVoltaMs
) { }
