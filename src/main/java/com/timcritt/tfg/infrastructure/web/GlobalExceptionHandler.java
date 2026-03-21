package com.timcritt.tfg.infrastructure.web;

import com.timcritt.tfg.application.exception.TestNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Spring MVC exception handler — lives in infrastructure/web because it's framework-specific (adapter layer).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TestNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTestNotFound(TestNotFoundException ex) {
        Map<String, Object> body = Map.of(
                "message", ex.getMessage(),
                "testId", ex.getTestId()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}

