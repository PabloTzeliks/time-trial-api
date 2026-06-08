package com.centroweg.iot.time_trial_api.outbound.web.controller;

import com.centroweg.iot.time_trial_api.core.service.SessaoAtualHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sessoes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SessaoController {

    private final SessaoAtualHolder sessaoAtualHolder;

    @PostMapping("/iniciar")
    public ResponseEntity<Map<String, String>> iniciarSessao() {
        String novaSessaoId = sessaoAtualHolder.iniciarNovaSessao();
        return ResponseEntity.ok(Map.of("sessaoId", novaSessaoId));
    }
}
