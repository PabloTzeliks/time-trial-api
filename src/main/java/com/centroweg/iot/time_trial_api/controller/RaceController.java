package com.centroweg.iot.time_trial_api.controller;

import com.centroweg.iot.time_trial_api.dto.RaceRequest;
import com.centroweg.iot.time_trial_api.dto.RaceResponse;
import com.centroweg.iot.time_trial_api.service.RaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/races")
@RequiredArgsConstructor
public class RaceController {

    private final RaceService raceService;

    @PostMapping
    public ResponseEntity<RaceResponse> create(@RequestBody RaceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(raceService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RaceResponse>> findAll() {
        return ResponseEntity.ok(raceService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RaceResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(raceService.findById(id));
    }

    @PutMapping("/{id}/start")
    public ResponseEntity<RaceResponse> start(@PathVariable UUID id) {
        return ResponseEntity.ok(raceService.start(id));
    }

    @PutMapping("/{id}/finish")
    public ResponseEntity<RaceResponse> finish(@PathVariable UUID id) {
        return ResponseEntity.ok(raceService.finish(id));
    }
}
