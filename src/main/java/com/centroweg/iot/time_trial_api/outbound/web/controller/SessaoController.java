package com.centroweg.iot.time_trial_api.outbound.web.controller;

import com.centroweg.iot.time_trial_api.core.service.SessaoAtualHolder;
import com.centroweg.iot.time_trial_api.outbound.web.dto.IniciarSessaoRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sessoes")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SessaoController {

    private final SessaoAtualHolder sessaoAtualHolder;

    @PostMapping("/iniciar")
    public ResponseEntity<Map<String, String>> iniciarSessao(
            @RequestBody(required = false) IniciarSessaoRequestDTO request) {

        String pistaId = request != null ? request.pistaId() : null;
        String novaSessaoId = sessaoAtualHolder.iniciarNovaSessao(pistaId);

        Map<String, String> resposta = new HashMap<>();
        resposta.put("sessaoId", novaSessaoId);
        resposta.put("pistaId", sessaoAtualHolder.snapshot().pistaId());
        return ResponseEntity.ok(resposta);
    }
}
