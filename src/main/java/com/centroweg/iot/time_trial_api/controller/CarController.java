package com.centroweg.iot.time_trial_api.controller;

import com.centroweg.iot.time_trial_api.dto.CarRequest;
import com.centroweg.iot.time_trial_api.dto.CarResponse;
import com.centroweg.iot.time_trial_api.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping
    public ResponseEntity<CarResponse> create(@RequestBody CarRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<CarResponse>> findAll() {
        return ResponseEntity.ok(carService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(carService.findById(id));
    }
}
