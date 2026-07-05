package com.jeewanavenue.config;

import java.time.format.DateTimeParseException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<?> handleInvalidFormat(InvalidFormatException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid value format",
                "message", ex.getOriginalMessage()
        ));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<?> handleDateTimeParse(DateTimeParseException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid date-time",
                "message", ex.getParsedString()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation failed",
                "details", ex.getBindingResult().getFieldErrors().stream()
                        .map(f -> f.getField() + ": " + f.getDefaultMessage())
                        .toList()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", "Access Denied",
                "message", "You don't have permission to access this resource"
        ));
    }
}