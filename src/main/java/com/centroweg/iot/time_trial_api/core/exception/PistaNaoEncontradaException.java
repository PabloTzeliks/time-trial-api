package com.centroweg.iot.time_trial_api.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PistaNaoEncontradaException extends RuntimeException {

    public PistaNaoEncontradaException(String pistaId) {
        super("Pista não encontrada: " + pistaId);
    }
}
