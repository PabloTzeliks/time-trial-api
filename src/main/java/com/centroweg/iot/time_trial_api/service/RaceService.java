package com.centroweg.iot.time_trial_api.service;

import com.centroweg.iot.time_trial_api.domain.Race;
import com.centroweg.iot.time_trial_api.domain.RaceStatus;
import com.centroweg.iot.time_trial_api.dto.RaceRequest;
import com.centroweg.iot.time_trial_api.dto.RaceResponse;
import com.centroweg.iot.time_trial_api.repository.RaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RaceService {

    private final RaceRepository raceRepository;

    public RaceResponse create(RaceRequest request) {
        Race race = Race.builder()
                .raceId(UUID.randomUUID())
                .name(request.getName())
                .totalLaps(request.getTotalLaps())
                .status(RaceStatus.SCHEDULED.name())
                .createdAt(Instant.now())
                .build();
        raceRepository.save(race);
        return toResponse(race);
    }

    public List<RaceResponse> findAll() {
        return raceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public RaceResponse findById(UUID id) {
        return raceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Race not found: " + id));
    }

    public RaceResponse start(UUID id) {
        Race race = raceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Race not found: " + id));
        race.setStatus(RaceStatus.ONGOING.name());
        race.setStartTime(Instant.now());
        raceRepository.save(race);
        return toResponse(race);
    }

    public RaceResponse finish(UUID id) {
        Race race = raceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Race not found: " + id));
        race.setStatus(RaceStatus.FINISHED.name());
        race.setEndTime(Instant.now());
        raceRepository.save(race);
        return toResponse(race);
    }

    private RaceResponse toResponse(Race race) {
        return RaceResponse.builder()
                .raceId(race.getRaceId())
                .name(race.getName())
                .totalLaps(race.getTotalLaps())
                .status(race.getStatus())
                .startTime(race.getStartTime())
                .endTime(race.getEndTime())
                .createdAt(race.getCreatedAt())
                .build();
    }
}
