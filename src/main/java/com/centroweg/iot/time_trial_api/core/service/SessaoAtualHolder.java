package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.Pista;
import com.centroweg.iot.time_trial_api.core.event.SessaoIniciadaEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class SessaoAtualHolder {

    private final ApplicationEventPublisher eventPublisher;
    private final PistaService pistaService;

    private final long tempoMinimoPadrao;
    private final long tempoMaximoPadrao;

    private final AtomicReference<EstadoSessao> estado;

    public SessaoAtualHolder(
            ApplicationEventPublisher eventPublisher,
            PistaService pistaService,
            @Value("${time-trial.secret-keys.tempo-minimo-volta}") long tempoMinimoPadrao,
            @Value("${time-trial.secret-keys.tempo-maximo-volta}") long tempoMaximoPadrao) {
        this.eventPublisher = eventPublisher;
        this.pistaService = pistaService;
        this.tempoMinimoPadrao = tempoMinimoPadrao;
        this.tempoMaximoPadrao = tempoMaximoPadrao;
        this.estado = new AtomicReference<>(sessaoComPadrao());
    }

    /** Snapshot atômico do estado da sessão — sessaoId e thresholds sempre consistentes entre si. */
    public EstadoSessao snapshot() {
        return estado.get();
    }

    public String getSessaoId() {
        return estado.get().sessaoId();
    }

    /** Reinicia a sessão sem vínculo a pista — usa os thresholds padrão do application.yaml. */
    public String iniciarNovaSessao() {
        return aplicar(sessaoComPadrao());
    }

    /**
     * Reinicia a sessão vinculada a uma pista; os thresholds passam a ser os dela.
     * Se pistaId for nulo/vazio, equivale a {@link #iniciarNovaSessao()}.
     */
    public String iniciarNovaSessao(String pistaId) {
        if (pistaId == null || pistaId.isBlank()) {
            return iniciarNovaSessao();
        }

        Pista pista = pistaService.buscar(pistaId);
        long minimo = pista.getTempoMinimoMs() != null ? pista.getTempoMinimoMs() : tempoMinimoPadrao;
        long maximo = pista.getTempoMaximoMs() != null ? pista.getTempoMaximoMs() : tempoMaximoPadrao;

        return aplicar(new EstadoSessao(UUID.randomUUID().toString(), pista.getId(), pista.getNome(), minimo, maximo));
    }

    private String aplicar(EstadoSessao nova) {
        estado.set(nova);
        log.info("Nova sessão iniciada: {} (pista: {}, tempos: [{}ms, {}ms])",
                nova.sessaoId(), nova.pistaId(), nova.tempoMinimoMs(), nova.tempoMaximoMs());
        eventPublisher.publishEvent(new SessaoIniciadaEvent(nova.sessaoId(), nova.pistaId(), nova.nomePista()));
        return nova.sessaoId();
    }

    private EstadoSessao sessaoComPadrao() {
        return new EstadoSessao(UUID.randomUUID().toString(), null, null, tempoMinimoPadrao, tempoMaximoPadrao);
    }

    public record EstadoSessao(String sessaoId, String pistaId, String nomePista, long tempoMinimoMs, long tempoMaximoMs) { }
}
