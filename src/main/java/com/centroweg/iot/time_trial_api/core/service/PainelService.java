package com.centroweg.iot.time_trial_api.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PainelService {

    private final PainelStateCache painelStateCache;

    public PainelSaidaDTO derivar() {
        return painelStateCache.snapshot();
    }
}
