package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.HistoricoCarro;
import com.centroweg.iot.time_trial_api.core.event.CarroPassouNoSensorEvent;
import com.centroweg.iot.time_trial_api.core.repository.JpaHistoricoCarroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculadoraDeVoltaService {

    private final JpaHistoricoCarroRepository historicoRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final Long TEMPO_MINIMO_VOLTA_MS = 2000L;
    private static final Long TEMPO_MAXIMO_VOLTA_MS = 30000L;

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

        if (tempoDaVolta < TEMPO_MINIMO_VOLTA_MS) {
            log.warn("Bounce/Ignorado - Carro {} - {}ms", rfid, tempoDaVolta);
            return;
        }

        if (tempoDaVolta > TEMPO_MAXIMO_VOLTA_MS) {
            log.warn("DNF/Timeout - Carro {} - Reiniciando volta.", rfid);
            salvarMarcoZero(rfid, timestampAtual);
            return;
        }

        // Volta Válida!
        log.info("Volta Concluída! Carro {} em {}ms", rfid, tempoDaVolta);
        salvarMarcoZero(rfid, timestampAtual); // O fim dessa é o início da próxima

        // Grita para o sistema que temos um tempo oficial!
        eventPublisher.publishEvent(new VoltaValidaCalculadaEvent(rfid, tempoDaVolta));
    }

    private void salvarMarcoZero(String rfid, Long timestamp) {
        HistoricoCarro hc = new HistoricoCarro();
        hc.setCarroId(rfid);
        hc.setTimestampMs(timestamp);
        historicoRepository.save(hc);
    }
}
