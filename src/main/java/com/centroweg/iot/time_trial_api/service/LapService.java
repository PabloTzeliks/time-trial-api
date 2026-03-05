package com.centroweg.iot.time_trial_api.service;

import com.centroweg.iot.time_trial_api.domain.LapTime;
import com.centroweg.iot.time_trial_api.domain.LapTimePrimaryKey;
import com.centroweg.iot.time_trial_api.dto.LapTimeMessage;
import com.centroweg.iot.time_trial_api.dto.LapTimeResponse;
import com.centroweg.iot.time_trial_api.repository.LapTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LapService {

    private final LapTimeRepository lapTimeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Persists a lap time received via MQTT and broadcasts it over WebSocket.
     */
    public LapTimeResponse recordLap(LapTimeMessage message) {
        LapTimePrimaryKey key = LapTimePrimaryKey.builder()
                .raceId(message.getRaceId())
                .carId(message.getCarId())
                .lapNumber(message.getLapNumber())
                .build();

        LapTime lapTime = LapTime.builder()
                .key(key)
                .timeMillis(message.getTimeMillis())
                .recordedAt(Instant.now())
                .build();

        lapTimeRepository.save(lapTime);

        LapTimeResponse response = toResponse(lapTime);

        // Broadcast to all subscribers of the general laps topic
        messagingTemplate.convertAndSend("/topic/laps", response);

        // Broadcast to race-specific topic
        messagingTemplate.convertAndSend(
                "/topic/race/" + message.getRaceId() + "/laps", response);

        return response;
    }

    public List<LapTimeResponse> findByRaceId(UUID raceId) {
        return lapTimeRepository.findByRaceId(raceId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LapTimeResponse> findByRaceIdAndCarId(UUID raceId, UUID carId) {
        return lapTimeRepository.findByRaceIdAndCarId(raceId, carId).stream()
                .map(this::toResponse)
                .toList();
    }

    private LapTimeResponse toResponse(LapTime lapTime) {
        return LapTimeResponse.builder()
                .raceId(lapTime.getKey().getRaceId())
                .carId(lapTime.getKey().getCarId())
                .lapNumber(lapTime.getKey().getLapNumber())
                .timeMillis(lapTime.getTimeMillis())
                .recordedAt(lapTime.getRecordedAt())
                .build();
    }
}
