package com.centroweg.iot.time_trial_api.core.api;

public record VoltaFeedDTO(
        String carroId,
        Long duracaoMs,
        Long ts,
        boolean isPessoalRecord
) { }
