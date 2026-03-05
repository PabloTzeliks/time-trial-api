package com.centroweg.iot.time_trial_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response object for lap time data.
 * Published over WebSocket to /topic/laps and /topic/race/{raceId}/laps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LapTimeResponse {
    private UUID raceId;
    private UUID carId;
    private Integer lapNumber;
    private Long timeMillis;
    private Instant recordedAt;
}
