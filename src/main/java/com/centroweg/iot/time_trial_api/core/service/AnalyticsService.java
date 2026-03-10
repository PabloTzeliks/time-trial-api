package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.exception.RecursoNaoEncontradoException;
import com.centroweg.iot.time_trial_api.core.mapper.HistoricoCarroMapper;
import com.centroweg.iot.time_trial_api.core.repository.JpaHistoricoCarroRepository;
import com.centroweg.iot.time_trial_api.outbound.dto.HistoricoCarroResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalyticsService {

    private final JpaHistoricoCarroRepository historicoCarroRepository;
    private final HistoricoCarroMapper historicoCarroMapper;

    public AnalyticsService(JpaHistoricoCarroRepository historicoCarroRepository, HistoricoCarroMapper historicoCarroMapper) {
        this.historicoCarroRepository = historicoCarroRepository;
        this.historicoCarroMapper = historicoCarroMapper;
    }

    public List<HistoricoCarroResponseDTO> listaHistoricoCarro() {

        var historicoCarros = historicoCarroRepository.findAll();

        if (historicoCarros.isEmpty()) {

            throw new RecursoNaoEncontradoException("Nenhum histórico de Carro encontrado.");
        }

        return historicoCarros.stream()
                .map(historicoCarroMapper::toDto)
                .toList();
    }
}
