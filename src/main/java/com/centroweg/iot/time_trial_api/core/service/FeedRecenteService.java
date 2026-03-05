package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.FeedRecente;
import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import com.centroweg.iot.time_trial_api.core.repository.JpaFeedRecenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedRecenteService {

    private final JpaFeedRecenteRepository feedRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void onVoltaValida(VoltaValidaCalculadaEvent evento) {
        String rfid = evento.rfid();
        Long tempoVolta = evento.tempoVoltaMs();

        log.info("Salvando volta no feed recente: Carro {} - {}ms", rfid, tempoVolta);

        FeedRecente feed = new FeedRecente();
        feed.setAgrupador("GERAL");
        feed.setTimestampMs(System.currentTimeMillis());
        feed.setCarroId(rfid);
        feed.setTempoVoltaMs(tempoVolta);

        feedRepository.save(feed);

        eventPublisher.publishEvent(new PainelPrecisaAtualizarEvent());
    }
}
