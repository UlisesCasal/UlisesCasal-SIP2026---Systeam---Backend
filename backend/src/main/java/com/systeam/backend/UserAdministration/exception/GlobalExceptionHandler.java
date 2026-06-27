package com.systeam.backend.UserAdministration.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("Error en la operacion (RuntimeException)", ex);
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error en la operación";
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "Recurso no encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", msg));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        log.warn("Conflicto en la operación: {}", ex.getMessage());
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "Conflicto en la operación";
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("error", msg));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(org.springframework.security.core.AuthenticationException ex) {
        log.warn("Error de autenticacion: {}", ex.getMessage());
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "Credenciales incorrectas";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("Error no manejado", ex);
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "Error interno del servidor";
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", msg));
    }

}