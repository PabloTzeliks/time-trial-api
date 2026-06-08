package com.centroweg.iot.time_trial_api.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class SessaoAtualHolder {

    private final AtomicReference<String> sessaoId = new AtomicReference<>(gerarNovaSessao());

    public String getSessaoId() {
        return sessaoId.get();
    }

    public String iniciarNovaSessao() {
        String nova = gerarNovaSessao();
        sessaoId.set(nova);
        log.info("Nova sessão iniciada: {}", nova);
        return nova;
    }

    private static String gerarNovaSessao() {
        return UUID.randomUUID().toString();
    }
}
