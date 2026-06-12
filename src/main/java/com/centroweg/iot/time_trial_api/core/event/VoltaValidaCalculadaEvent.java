package com.centroweg.iot.time_trial_api.core.event;

public record VoltaValidaCalculadaEvent(
        String sessaoId,
        String rfid,
        Long duracaoMs,
        Long ts
) { }
