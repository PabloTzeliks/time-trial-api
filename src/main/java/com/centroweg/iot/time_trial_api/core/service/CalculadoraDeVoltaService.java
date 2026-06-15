package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.event.CarroPassouNoSensorEvent;
import com.centroweg.iot.time_trial_api.core.event.SessaoIniciadaEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculadoraDeVoltaService {

    private final ApplicationEventPublisher eventPublisher;
    private final SessaoAtualHolder sessaoAtualHolder;

    private final ConcurrentHashMap<String, MarcoZero> marcoZero = new ConcurrentHashMap<>();

    @Async("eventExecutor")
    @EventListener
    public void onCarroPassou(CarroPassouNoSensorEvent evento) {
        String rfid = evento.rfid();
        Long tsAtual = evento.timestampMs();

        SessaoAtualHolder.EstadoSessao sessao = sessaoAtualHolder.snapshot();
        String sessaoAtual = sessao.sessaoId();
        long tempoMinimoVolta = sessao.tempoMinimoMs();
        long tempoMaximoVolta = sessao.tempoMaximoMs();

        AtomicReference<Decisao> decisao = new AtomicReference<>(Decisao.PRIMEIRA_PASSAGEM);
        AtomicReference<Long> duracaoCalculada = new AtomicReference<>(0L);

        marcoZero.compute(rfid, (key, prev) -> {
            if (prev == null || !prev.sessaoId().equals(sessaoAtual)) {
                return new MarcoZero(sessaoAtual, tsAtual);
            }

            long duracao = tsAtual - prev.ts();

            if (duracao < tempoMinimoVolta) {
                decisao.set(Decisao.BOUNCE);
                duracaoCalculada.set(duracao);
                return prev;
            }

            if (duracao > tempoMaximoVolta) {
                decisao.set(Decisao.DNF);
                return new MarcoZero(sessaoAtual, tsAtual);
            }

            decisao.set(Decisao.VOLTA_VALIDA);
            duracaoCalculada.set(duracao);
            return new MarcoZero(sessaoAtual, tsAtual);
        });

        switch (decisao.get()) {
            case PRIMEIRA_PASSAGEM -> log.info("Novo carro na pista: {}", rfid);
            case BOUNCE -> log.warn("Bounce ignorado — carro {} em {}ms (abaixo do mínimo)", rfid, duracaoCalculada.get());
            case DNF -> log.warn("DNF — carro {} excedeu tempo máximo, marco reiniciado", rfid);
            case VOLTA_VALIDA -> {
                log.info("Volta válida — carro {} em {}ms", rfid, duracaoCalculada.get());
                eventPublisher.publishEvent(new VoltaValidaCalculadaEvent(
                        sessaoAtual,
                        rfid,
                        duracaoCalculada.get(),
                        tsAtual
                ));
            }
        }
    }

    @EventListener
    public void onSessaoIniciada(SessaoIniciadaEvent event) {
        marcoZero.clear();
        log.info("marcoZero purgado após início da sessão {}", event.sessaoId());
    }

    private enum Decisao {
        PRIMEIRA_PASSAGEM, BOUNCE, DNF, VOLTA_VALIDA
    }

    private record MarcoZero(String sessaoId, Long ts) { }
}
