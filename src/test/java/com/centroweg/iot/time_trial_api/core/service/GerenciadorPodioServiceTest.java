package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.PodioGlobal;
import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import com.centroweg.iot.time_trial_api.core.repository.JpaPodioGlobalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerenciadorPodioServiceTest {

    @Mock
    private JpaPodioGlobalRepository podioRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GerenciadorPodioService gerenciadorPodioService;

    private PodioGlobal criarPodio(String carroId, Long tempo) {
        PodioGlobal p = new PodioGlobal();
        p.setAgrupador("GERAL");
        p.setCarroId(carroId);
        p.setTempoVoltaMs(tempo);
        return p;
    }

    @Test
    void onVoltaValida_carNotOnPodium_vacantSpot_shouldInsert() {
        List<PodioGlobal> top10 = new ArrayList<>();
        top10.add(criarPodio("CAR-A", 3000L));
        when(podioRepository.buscarTop10()).thenReturn(top10);

        gerenciadorPodioService.onVoltaValida(new VoltaValidaCalculadaEvent("CAR-B", 4000L));

        ArgumentCaptor<PodioGlobal> captor = ArgumentCaptor.forClass(PodioGlobal.class);
        verify(podioRepository).save(captor.capture());
        assertEquals("CAR-B", captor.getValue().getCarroId());
        assertEquals(4000L, captor.getValue().getTempoVoltaMs());
        verify(podioRepository, never()).delete(any());
    }

    @Test
    void onVoltaValida_carAlreadyOnPodium_fasterTime_shouldUpdate() {
        PodioGlobal existing = criarPodio("CAR-A", 5000L);
        List<PodioGlobal> top10 = new ArrayList<>();
        top10.add(existing);
        when(podioRepository.buscarTop10()).thenReturn(top10);

        gerenciadorPodioService.onVoltaValida(new VoltaValidaCalculadaEvent("CAR-A", 3000L));

        verify(podioRepository).delete(existing);
        ArgumentCaptor<PodioGlobal> captor = ArgumentCaptor.forClass(PodioGlobal.class);
        verify(podioRepository).save(captor.capture());
        assertEquals("CAR-A", captor.getValue().getCarroId());
        assertEquals(3000L, captor.getValue().getTempoVoltaMs());
    }

    @Test
    void onVoltaValida_carAlreadyOnPodium_slowerTime_shouldNotUpdate() {
        PodioGlobal existing = criarPodio("CAR-A", 3000L);
        List<PodioGlobal> top10 = new ArrayList<>();
        top10.add(existing);
        when(podioRepository.buscarTop10()).thenReturn(top10);

        gerenciadorPodioService.onVoltaValida(new VoltaValidaCalculadaEvent("CAR-A", 5000L));

        verify(podioRepository, never()).delete(any());
        verify(podioRepository, never()).save(any());
    }

    @Test
    void onVoltaValida_podiumFull_fasterThanLast_shouldExpelLast() {
        List<PodioGlobal> top10 = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            top10.add(criarPodio("CAR-" + i, (long) (i * 1000)));
        }
        when(podioRepository.buscarTop10()).thenReturn(top10);

        // New car with time 5000ms is faster than 10th place (10000ms)
        gerenciadorPodioService.onVoltaValida(new VoltaValidaCalculadaEvent("CAR-NEW", 5000L));

        // Should delete the 10th place entry
        verify(podioRepository).delete(top10.get(9));
        // Should insert the new car
        ArgumentCaptor<PodioGlobal> captor = ArgumentCaptor.forClass(PodioGlobal.class);
        verify(podioRepository).save(captor.capture());
        assertEquals("CAR-NEW", captor.getValue().getCarroId());
        assertEquals(5000L, captor.getValue().getTempoVoltaMs());
    }

    @Test
    void onVoltaValida_podiumFull_slowerThanLast_shouldNotInsert() {
        List<PodioGlobal> top10 = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            top10.add(criarPodio("CAR-" + i, (long) (i * 1000)));
        }
        when(podioRepository.buscarTop10()).thenReturn(top10);

        // New car with time 15000ms is slower than 10th place (10000ms)
        gerenciadorPodioService.onVoltaValida(new VoltaValidaCalculadaEvent("CAR-NEW", 15000L));

        verify(podioRepository, never()).delete(any());
        verify(podioRepository, never()).save(any());
    }

    @Test
    void onVoltaValida_shouldAlwaysPublishPainelEvent() {
        when(podioRepository.buscarTop10()).thenReturn(new ArrayList<>());

        gerenciadorPodioService.onVoltaValida(new VoltaValidaCalculadaEvent("CAR-A", 5000L));

        verify(eventPublisher).publishEvent(any(PainelPrecisaAtualizarEvent.class));
    }
}
