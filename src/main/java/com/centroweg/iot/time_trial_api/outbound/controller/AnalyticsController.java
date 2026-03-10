package com.centroweg.iot.time_trial_api.outbound.controller;

import com.centroweg.iot.time_trial_api.core.service.AnalyticsService;
import com.centroweg.iot.time_trial_api.outbound.dto.HistoricoCarroResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<List<HistoricoCarroResponseDTO>> getAnalytics() {

        return ResponseEntity.ok(analyticsService.listaHistoricoCarro());
    }
}
