package com.centroweg.iot.time_trial_api.outbound.dto;

import com.centroweg.iot.time_trial_api.core.domain.FeedRecente;
import com.centroweg.iot.time_trial_api.core.domain.PodioGlobal;

import java.util.List;

public record PainelSaidaDTO(
        List<PodioGlobal> podio,
        List<FeedRecente> recentes
) { }
