package com.centroweg.iot.time_trial_api.controller;

import com.centroweg.iot.time_trial_api.dto.LapTimeResponse;
import com.centroweg.iot.time_trial_api.service.LapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/races")
@RequiredArgsConstructor
public class LapController {

    private final LapService lapService;

    @GetMapping("/{raceId}/laps")
    public ResponseEntity<List<LapTimeResponse>> findByRace(@PathVariable UUID raceId) {
        return ResponseEntity.ok(lapService.findByRaceId(raceId));
    }

    @GetMapping("/{raceId}/laps/car/{carId}")
    public ResponseEntity<List<LapTimeResponse>> findByRaceAndCar(
            @PathVariable UUID raceId,
            @PathVariable UUID carId) {
        return ResponseEntity.ok(lapService.findByRaceIdAndCarId(raceId, carId));
    }
}
