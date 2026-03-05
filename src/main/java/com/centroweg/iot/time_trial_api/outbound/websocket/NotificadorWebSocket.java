package com.centroweg.iot.time_trial_api.outbound.websocket;

import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.repository.JpaFeedRecenteRepository;
import com.centroweg.iot.time_trial_api.core.repository.JpaPodioGlobalRepository;
import com.centroweg.iot.time_trial_api.outbound.dto.PainelSaidaDTO;
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
    private final JpaPodioGlobalRepository podioRepository;
    private final JpaFeedRecenteRepository feedRepository;

    @Async
    @EventListener
    public void onPainelPrecisaAtualizar(PainelPrecisaAtualizarEvent event) {
        log.info("Preparando atualização de painel via WebSocket...");

        var top10 = podioRepository.buscarTop10();
        var ultimasVoltas = feedRepository.buscarUltimas10();

        PainelSaidaDTO payload = new PainelSaidaDTO(top10, ultimasVoltas);

        messagingTemplate.convertAndSend("/topic/painel", payload);

        log.info("Painel enviado com sucesso!");
    }
}
