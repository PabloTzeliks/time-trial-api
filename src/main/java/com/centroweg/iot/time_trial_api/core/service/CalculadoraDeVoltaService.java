package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.HistoricoCarro;
import com.centroweg.iot.time_trial_api.core.event.CarroPassouNoSensorEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import com.centroweg.iot.time_trial_api.core.repository.JpaHistoricoCarroRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CalculadoraDeVoltaService {

    private final JpaHistoricoCarroRepository historicoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value(value = "${time-trial.secret-keys.tempo-minimo-volta}")
    private Long tempoMinimoVolta;

    @Value(value = "${time-trial.secret-keys.tempo-maximo-volta}")
    private Long tempoMaximoVolta;

    public CalculadoraDeVoltaService(JpaHistoricoCarroRepository historicoRepository, ApplicationEventPublisher eventPublisher) {
        this.historicoRepository = historicoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Async
    @EventListener
    public void onCarroPassou(CarroPassouNoSensorEvent evento) {

        String rfid = evento.rfid();
        Long timestampAtual = evento.timestampMs();

        HistoricoCarro ultimaPassagem = historicoRepository.findFirstByCarroId(rfid);

        if (ultimaPassagem == null) {
            log.info("Novo carro na pista: {}", rfid);
            salvarMarcoZero(rfid, timestampAtual);
            return;
        }

        Long tempoDaVolta = timestampAtual - ultimaPassagem.getTimestampMs();

        if (tempoDaVolta < tempoMinimoVolta) {

            log.warn("Bounce/Ignorado - Carro {} - {}ms", rfid, tempoDaVolta);
            return;
        }

        if (tempoDaVolta > tempoMaximoVolta) {

            log.warn("DNF/Timeout - Carro {} - Reiniciando volta.", rfid);
            salvarMarcoZero(rfid, timestampAtual);
            return;
        }

        log.info("Volta Concluída! Carro {} em {}ms", rfid, tempoDaVolta);
        salvarMarcoZero(rfid, timestampAtual);

        eventPublisher.publishEvent(new VoltaValidaCalculadaEvent(rfid, tempoDaVolta));
    }

    private void salvarMarcoZero(String rfid, Long timestamp) {

        HistoricoCarro hc = new HistoricoCarro();
        hc.setCarroId(rfid);
        hc.setTimestampMs(timestamp);

        historicoRepository.save(hc);
    }
}
