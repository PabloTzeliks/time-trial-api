package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.Pista;
import com.centroweg.iot.time_trial_api.core.exception.PistaNaoEncontradaException;
import com.centroweg.iot.time_trial_api.core.exception.ValidacaoException;
import com.centroweg.iot.time_trial_api.core.repository.PistaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PistaService {

    private final PistaRepository pistaRepository;

    public Pista criar(String nome, Long tempoMinimoMs, Long tempoMaximoMs) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("Nome da pista é obrigatório");
        }
        if (tempoMinimoMs == null || tempoMinimoMs <= 0) {
            throw new ValidacaoException("tempoMinimoMs deve ser positivo");
        }
        if (tempoMaximoMs == null || tempoMaximoMs <= tempoMinimoMs) {
            throw new ValidacaoException("tempoMaximoMs deve ser maior que tempoMinimoMs");
        }

        Pista pista = new Pista();
        pista.setId(UUID.randomUUID().toString());
        pista.setNome(nome.trim());
        pista.setTempoMinimoMs(tempoMinimoMs);
        pista.setTempoMaximoMs(tempoMaximoMs);

        Pista salva = pistaRepository.save(pista);
        log.info("Pista criada — {} ({}) [{}ms, {}ms]", salva.getNome(), salva.getId(), salva.getTempoMinimoMs(), salva.getTempoMaximoMs());
        return salva;
    }

    public List<Pista> listar() {
        return pistaRepository.findAll();
    }

    public Pista buscar(String id) {
        return pistaRepository.findById(id)
                .orElseThrow(() -> new PistaNaoEncontradaException(id));
    }
}
