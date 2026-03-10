package com.centroweg.iot.time_trial_api.outbound.web.common;

import com.centroweg.iot.time_trial_api.core.domain.exception.RecursoNaoEncontradoException;
import com.centroweg.iot.time_trial_api.outbound.dto.error.RespostaErro;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> handleResourceNotFound(RecursoNaoEncontradoException ex, HttpServletRequest request) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> handleGenericException(Exception ex, HttpServletRequest request) {

        LOGGER.error("Unexpected error occurred at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );
    }

    private ResponseEntity<RespostaErro> buildErrorResponse(

            HttpStatus status,
            String title,
            String detail,
            String instance
    ) {

        RespostaErro response = new RespostaErro(

                "about:blank",
                title,
                status.value(),
                detail,
                instance,
                LocalDateTime.now()
        );

        return ResponseEntity.status(status).body(response);
    }
}