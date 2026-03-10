package com.centroweg.iot.time_trial_api.outbound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HistoricoCarroResponseDTO(

        @JsonProperty("rfid") String rfid,
        @JsonProperty("timestamp_ms") Long timestampMs
) { }
