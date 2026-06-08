package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.Volta;
import com.centroweg.iot.time_trial_api.core.repository.VoltaRepository;
import com.centroweg.iot.time_trial_api.outbound.dto.LeaderboardEntryDTO;
import com.centroweg.iot.time_trial_api.outbound.dto.PainelSaidaDTO;
import com.centroweg.iot.time_trial_api.outbound.dto.VoltaFeedDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PainelService {

    private final VoltaRepository voltaRepository;
    private final SessaoAtualHolder sessaoAtualHolder;

    public PainelSaidaDTO derivar() {
        List<Volta> voltas = voltaRepository.findBySessaoId(sessaoAtualHolder.getSessaoId());
        return new PainelSaidaDTO(buildLeaderboard(voltas), buildFeed(voltas));
    }

    private List<LeaderboardEntryDTO> buildLeaderboard(List<Volta> voltas) {
        AtomicInteger posicao = new AtomicInteger(1);

        return voltas.stream()
                .collect(java.util.stream.Collectors.toMap(
                        Volta::getCarroId,
                        Volta::getDuracaoMs,
                        Long::min
                ))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByValue())
                .limit(10)
                .map(e -> new LeaderboardEntryDTO(posicao.getAndIncrement(), e.getKey(), e.getValue()))
                .toList();
    }

    private List<VoltaFeedDTO> buildFeed(List<Volta> voltas) {
        return voltas.stream()
                .sorted(Comparator.comparingLong(Volta::getTs).reversed())
                .limit(10)
                .map(v -> new VoltaFeedDTO(v.getCarroId(), v.getDuracaoMs(), v.getTs()))
                .toList();
    }
}
