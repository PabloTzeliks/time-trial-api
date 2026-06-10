package com.centroweg.iot.time_trial_api.core.service;

import java.util.List;

public record PainelSaidaDTO(
        List<LeaderboardEntryDTO> leaderboard,
        List<VoltaFeedDTO> recentes
) { }
