package com.centroweg.iot.time_trial_api.core.service;

public record LeaderboardEntryDTO(
        int posicao,
        String carroId,
        Long duracaoMs
) { }
