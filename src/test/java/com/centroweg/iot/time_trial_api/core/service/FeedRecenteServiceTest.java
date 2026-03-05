package com.centroweg.iot.time_trial_api.core.service;

import com.centroweg.iot.time_trial_api.core.domain.FeedRecente;
import com.centroweg.iot.time_trial_api.core.event.PainelPrecisaAtualizarEvent;
import com.centroweg.iot.time_trial_api.core.event.VoltaValidaCalculadaEvent;
import com.centroweg.iot.time_trial_api.core.repository.JpaFeedRecenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedRecenteServiceTest {

    @Mock
    private JpaFeedRecenteRepository feedRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FeedRecenteService feedRecenteService;

    @Test
    void onVoltaValida_shouldSaveFeedRecenteAndPublishPainelEvent() {
        VoltaValidaCalculadaEvent evento = new VoltaValidaCalculadaEvent("RFID-001", 5000L);

        feedRecenteService.onVoltaValida(evento);

        ArgumentCaptor<FeedRecente> feedCaptor = ArgumentCaptor.forClass(FeedRecente.class);
        verify(feedRepository).save(feedCaptor.capture());

        FeedRecente saved = feedCaptor.getValue();
        assertEquals("GERAL", saved.getAgrupador());
        assertEquals("RFID-001", saved.getCarroId());
        assertEquals(5000L, saved.getTempoVoltaMs());
        assertNotNull(saved.getTimestampMs());

        verify(eventPublisher).publishEvent(any(PainelPrecisaAtualizarEvent.class));
    }
}
