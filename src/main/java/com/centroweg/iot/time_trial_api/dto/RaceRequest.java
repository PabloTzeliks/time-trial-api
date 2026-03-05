package com.centroweg.iot.time_trial_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceRequest {
    private String name;
    private Integer totalLaps;
}
