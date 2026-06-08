package com.centroweg.iot.time_trial_api.outbound.dto;

public record VoltaFeedDTO(
        String carroId,
        Long duracaoMs,
        Long ts
) { }
