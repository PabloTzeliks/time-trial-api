package com.centroweg.iot.time_trial_api.outbound.websocket;

import com.centroweg.iot.time_trial_api.core.api.PainelSaidaDTO;
import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.service.PainelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificadorWebSocket {

    private final SimpMessagingTemplate messagingTemplate;
    private final PainelService painelService;

    @Async("notifierExecutor")
    @EventListener
    public void onPainelPrecisaAtualizar(PainelPrecisaAtualizarEvent event) {
        PainelSaidaDTO payload = painelService.derivar();
        messagingTemplate.convertAndSend("/topic/painel", payload);
        log.info("Painel enviado — sessão {} pista '{}' leaderboard {} entradas, feed {} entradas",
                payload.sessaoId(), payload.nomePista(), payload.leaderboard().size(), payload.recentes().size());
    }
}
