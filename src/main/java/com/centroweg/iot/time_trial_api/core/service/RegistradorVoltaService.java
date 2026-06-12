package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.Volta;
import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import com.centroweg.iot.time_trial_api.core.repository.VoltaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistradorVoltaService {

    private final VoltaRepository voltaRepository;
    private final PainelStateCache painelStateCache;
    private final ApplicationEventPublisher eventPublisher;

    @Async("eventExecutor")
    @EventListener
    public void onVoltaValida(VoltaValidaCalculadaEvent evento) {
        Volta volta = new Volta();
        volta.setSessaoId(evento.sessaoId());
        volta.setCarroId(evento.rfid());
        volta.setTs(evento.ts());
        volta.setDuracaoMs(evento.duracaoMs());

        voltaRepository.save(volta);

        boolean aceito = painelStateCache.registrar(evento.sessaoId(), evento.rfid(), evento.duracaoMs(), evento.ts());

        log.info("Volta persistida — sessão {} carro {} em {}ms", evento.sessaoId(), evento.rfid(), evento.duracaoMs());

        if (aceito) {
            eventPublisher.publishEvent(new PainelPrecisaAtualizarEvent());
        }
    }
}
