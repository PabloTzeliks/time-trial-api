package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.FeedRecente;
import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedRecenteService {

    private final CassandraOperations cassandraTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @EventListener
    public void onVoltaValida(VoltaValidaCalculadaEvent evento) {
        String rfid = evento.rfid();
        Long tempoVolta = evento.tempoVoltaMs();

        log.info("Salvando volta no feed recente com TTL: Carro {} - {}ms", rfid, tempoVolta);

        FeedRecente feed = new FeedRecente();
        feed.setAgrupador("GERAL");
        feed.setTimestampMs(System.currentTimeMillis());
        feed.setCarroId(rfid);
        feed.setTempoVoltaMs(tempoVolta);

        // A Mágica acontece aqui: Salvamos com TTL de 60 segundos
        InsertOptions options = InsertOptions.builder().ttl(Duration.ofSeconds(60)).build();
        cassandraTemplate.insert(feed, options);

        eventPublisher.publishEvent(new PainelPrecisaAtualizarEvent());
    }
}
