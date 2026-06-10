package com.centroweg.iot.time_trial_api.core.service;

public record VoltaFeedDTO(
        String carroId,
        Long duracaoMs,
        Long ts
) { }
