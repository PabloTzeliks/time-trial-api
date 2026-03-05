package com.centroweg.iot.time_trial_api.service;

import com.centroweg.iot.time_trial_api.domain.Car;
import com.centroweg.iot.time_trial_api.dto.CarRequest;
import com.centroweg.iot.time_trial_api.dto.CarResponse;
import com.centroweg.iot.time_trial_api.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public CarResponse create(CarRequest request) {
        Car car = Car.builder()
                .carId(UUID.randomUUID())
                .name(request.getName())
                .driver(request.getDriver())
                .createdAt(Instant.now())
                .build();
        carRepository.save(car);
        return toResponse(car);
    }

    public List<CarResponse> findAll() {
        return carRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CarResponse findById(UUID id) {
        return carRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Car not found: " + id));
    }

    private CarResponse toResponse(Car car) {
        return CarResponse.builder()
                .carId(car.getCarId())
                .name(car.getName())
                .driver(car.getDriver())
                .createdAt(car.getCreatedAt())
                .build();
    }
}
