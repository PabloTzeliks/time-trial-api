package com.centroweg.iot.time_trial_api.outbound.dto;

public record LeaderboardEntryDTO(
        int posicao,
        String carroId,
        Long duracaoMs
) { }
