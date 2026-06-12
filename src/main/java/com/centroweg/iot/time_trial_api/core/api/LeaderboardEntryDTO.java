package com.centroweg.iot.time_trial_api.core.api;

public record LeaderboardEntryDTO(
        int posicao,
        String carroId,
        Long duracaoMs
) { }
