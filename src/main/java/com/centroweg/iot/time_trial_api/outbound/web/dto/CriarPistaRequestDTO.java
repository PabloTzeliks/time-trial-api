package com.centroweg.iot.time_trial_api.outbound.web.dto;

public record CriarPistaRequestDTO(
        String nome,
        Long tempoMinimoMs,
        Long tempoMaximoMs
) { }
