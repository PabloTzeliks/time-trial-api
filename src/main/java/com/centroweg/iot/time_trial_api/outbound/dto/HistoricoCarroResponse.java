package com.centroweg.iot.time_trial_api.outbound.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HistoricoCarroResponse(

        @JsonProperty("rfid") String rfid,
        @JsonProperty("timestamp_ms") Long timestampMs
) { }
