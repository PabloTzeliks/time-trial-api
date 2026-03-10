package com.centroweg.iot.time_trial_api.outbound.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespostaErro(

        String type,
        String title,
        int status,
        String detail,
        String instance,
        LocalDateTime time
) { }