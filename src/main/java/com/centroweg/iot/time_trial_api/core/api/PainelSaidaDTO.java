package com.centroweg.iot.time_trial_api.core.api;

import java.util.List;

public record PainelSaidaDTO(
        String sessaoId,
        String nomePista,
        List<LeaderboardEntryDTO> leaderboard,
        List<VoltaFeedDTO> recentes
) { }
