package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.PodioGlobal;
import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import com.centroweg.iot.time_trial_api.core.repository.JpaPodioGlobalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GerenciadorPodioService {

    private final JpaPodioGlobalRepository podioRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void onVoltaValida(VoltaValidaCalculadaEvent evento) {
        String rfid = evento.rfid();
        Long novoTempo = evento.tempoVoltaMs();

        List<PodioGlobal> top10 = podioRepository.buscarTop10();

        Optional<PodioGlobal> recordeExistente = top10.stream()
                .filter(p -> p.getCarroId().equals(rfid))
                .findFirst();

        if (recordeExistente.isPresent()) {

            if (novoTempo < recordeExistente.get().getTempoVoltaMs()) {

                log.info("Novo recorde pessoal para {}: {}ms", rfid, novoTempo);
                atualizarPosicao(recordeExistente.get(), novoTempo);
            }

        } else if (top10.size() < 10) {

            log.info("Carro {} entrou no pódio (vaga aberta): {}ms", rfid, novoTempo);
            inserirNovo(rfid, novoTempo);

        } else {

            PodioGlobal decimo = top10.get(top10.size() - 1);

            if (novoTempo < decimo.getTempoVoltaMs()) {

                log.info("Carro {} expulsou o 10º lugar!", rfid);
                podioRepository.delete(decimo);
                inserirNovo(rfid, novoTempo);
            }
        }

        eventPublisher.publishEvent(new PainelPrecisaAtualizarEvent());
    }

    private void atualizarPosicao(PodioGlobal antigo, Long novoTempo) {

        podioRepository.delete(antigo);

        inserirNovo(antigo.getCarroId(), novoTempo);
    }

    private void inserirNovo(String rfid, Long tempo) {

        PodioGlobal novo = new PodioGlobal();
        novo.setAgrupador("GERAL");
        novo.setCarroId(rfid);
        novo.setTempoVoltaMs(tempo);

        podioRepository.save(novo);
    }
}
