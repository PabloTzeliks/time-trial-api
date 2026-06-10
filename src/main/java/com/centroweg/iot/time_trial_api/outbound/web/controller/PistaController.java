package com.centroweg.iot.time_trial_api.outbound.web.controller;

import com.centroweg.iot.time_trial_api.core.domain.Pista;
import com.centroweg.iot.time_trial_api.core.service.PistaService;
import com.centroweg.iot.time_trial_api.outbound.web.dto.CriarPistaRequestDTO;
import com.centroweg.iot.time_trial_api.outbound.web.dto.PistaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pistas")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PistaController {

    private final PistaService pistaService;

    @PostMapping
    public ResponseEntity<PistaDTO> criar(@RequestBody CriarPistaRequestDTO request) {
        Pista pista = pistaService.criar(request.nome(), request.tempoMinimoMs(), request.tempoMaximoMs());
        return ResponseEntity.status(HttpStatus.CREATED).body(PistaDTO.de(pista));
    }

    @GetMapping
    public ResponseEntity<List<PistaDTO>> listar() {
        List<PistaDTO> pistas = pistaService.listar().stream()
                .map(PistaDTO::de)
                .toList();
        return ResponseEntity.ok(pistas);
    }
}
