package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.api.LeaderboardEntryDTO;
import com.centroweg.iot.time_trial_api.core.api.PainelSaidaDTO;
import com.centroweg.iot.time_trial_api.core.api.VoltaFeedDTO;
import com.centroweg.iot.time_trial_api.core.event.SessaoIniciadaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class PainelStateCache {

    private static final int FEED_CAPACITY = 10;
    private static final int LEADERBOARD_CAPACITY = 10;

    private final SessaoAtualHolder sessaoAtualHolder;

    private volatile String sessaoIdAtual = "";
    private volatile String nomePistaAtual = null;

    private final ConcurrentHashMap<String, Long> melhoresTempos = new ConcurrentHashMap<>();
    private final PriorityQueue<VoltaFeedDTO> feedHeap = new PriorityQueue<>(Comparator.comparingLong(VoltaFeedDTO::ts));
    private final Object feedLock = new Object();

    public void registrar(String sessaoId, String carroId, Long duracaoMs, Long ts) {
        if (!sessaoId.equals(sessaoAtualHolder.getSessaoId())) {
            log.warn("Registro descartado — pertence à sessão {} (atual: {})", sessaoId, sessaoAtualHolder.getSessaoId());
            return;
        }

        Long melhor = melhoresTempos.merge(carroId, duracaoMs, Long::min);
        boolean isPessoalRecord = melhor.equals(duracaoMs);

        VoltaFeedDTO entrada = new VoltaFeedDTO(carroId, duracaoMs, ts, isPessoalRecord);
        synchronized (feedLock) {
            if (feedHeap.size() < FEED_CAPACITY) {
                feedHeap.offer(entrada);
            } else if (ts > feedHeap.peek().ts()) {
                feedHeap.poll();
                feedHeap.offer(entrada);
            }
        }
    }

    public PainelSaidaDTO snapshot() {
        AtomicInteger posicao = new AtomicInteger(1);
        List<LeaderboardEntryDTO> leaderboard = melhoresTempos.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(LEADERBOARD_CAPACITY)
                .map(e -> new LeaderboardEntryDTO(posicao.getAndIncrement(), e.getKey(), e.getValue()))
                .toList();

        List<VoltaFeedDTO> feed;
        synchronized (feedLock) {
            feed = new ArrayList<>(feedHeap);
        }
        feed.sort(Comparator.comparingLong(VoltaFeedDTO::ts).reversed());

        return new PainelSaidaDTO(sessaoIdAtual, nomePistaAtual, leaderboard, feed);
    }

    @EventListener
    public void onSessaoIniciada(SessaoIniciadaEvent event) {
        this.sessaoIdAtual = event.sessaoId();
        this.nomePistaAtual = event.nomePista();
        melhoresTempos.clear();
        synchronized (feedLock) {
            feedHeap.clear();
        }
        log.info("PainelStateCache limpo após início da sessão {} (pista: {})", event.sessaoId(), event.nomePista());
    }
}
