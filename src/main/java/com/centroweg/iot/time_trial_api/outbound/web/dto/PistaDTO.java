package com.centroweg.iot.time_trial_api.outbound.web.dto;

import com.centroweg.iot.time_trial_api.core.domain.Pista;

public record PistaDTO(
        String id,
        String nome,
        Long tempoMinimoMs,
        Long tempoMaximoMs
) {
    public static PistaDTO de(Pista pista) {
        return new PistaDTO(pista.getId(), pista.getNome(), pista.getTempoMinimoMs(), pista.getTempoMaximoMs());
    }
}
