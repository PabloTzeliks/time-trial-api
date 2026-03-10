package com.centroweg.iot.time_trial_api.outbound.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(

        String type,
        String title,
        int status,
        String detail,
        String instance
) { }