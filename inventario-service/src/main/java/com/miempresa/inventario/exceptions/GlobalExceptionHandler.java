package com.miempresa.inventario.exceptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Object> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return new ResponseEntity<>(
                Map.of(
                        "errors", List.of(
                                Map.of(
                                        "status", "404",
                                        "title", "Recurso no encontrado",
                                        "detail", ex.getMessage(),
                                        "timestamp", LocalDateTime.now().toString()
                                )
                        )
                ),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> manejarValidaciones(MethodArgumentNotValidException ex) {
        List<Map<String, Object>> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> Map.of(
                        "status", "400",
                        "title", "Error de validación",
                        "detail", error.getDefaultMessage(),
                        "source", Map.of("pointer", "/data/attributes/" + error.getField()),
                        "timestamp", LocalDateTime.now().toString()
                ))
                .collect(Collectors.toList());

        return new ResponseEntity<>(Map.of("errors", errores), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> manejarViolaciones(ConstraintViolationException ex) {
        List<Map<String, Object>> errores = ex.getConstraintViolations().stream()
                .map(violation -> Map.of(
                        "status", "400",
                        "title", "Violación de restricción",
                        "detail", violation.getMessage(),
                        "source", Map.of("pointer", violation.getPropertyPath().toString()),
                        "timestamp", LocalDateTime.now().toString()
                ))
                .collect(Collectors.toList());

        return new ResponseEntity<>(Map.of("errors", errores), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> manejarExcepcionesGenerales(Exception ex) {
        return new ResponseEntity<>(
                Map.of(
                        "errors", List.of(
                                Map.of(
                                        "status", "500",
                                        "title", "Error interno del servidor",
                                        "detail", ex.getMessage(),
                                        "timestamp", LocalDateTime.now().toString()
                                )
                        )
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
