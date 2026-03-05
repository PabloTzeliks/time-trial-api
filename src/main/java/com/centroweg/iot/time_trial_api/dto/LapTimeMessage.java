package com.centroweg.iot.time_trial_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Payload published by the IoT sensor to the MQTT topic.
 * Topic: time_trial/lap_completed
 *
 * Example JSON:
 * {
 *   "carId": "550e8400-e29b-41d4-a716-446655440000",
 *   "raceId": "660e8400-e29b-41d4-a716-446655440001",
 *   "lapNumber": 3,
 *   "timeMillis": 4521
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LapTimeMessage {
    private UUID carId;
    private UUID raceId;
    private Integer lapNumber;
    private Long timeMillis;
}
