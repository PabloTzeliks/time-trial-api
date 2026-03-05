package com.centroweg.iot.time_trial_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RaceResponse {
    private UUID raceId;
    private String name;
    private Integer totalLaps;
    private String status;
    private Instant startTime;
    private Instant endTime;
    private Instant createdAt;
}
