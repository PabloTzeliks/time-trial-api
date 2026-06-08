package com.centroweg.iot.time_trial_api.outbound.dto;

import java.util.List;

public record PainelSaidaDTO(
        List<LeaderboardEntryDTO> leaderboard,
        List<VoltaFeedDTO> recentes
) { }
