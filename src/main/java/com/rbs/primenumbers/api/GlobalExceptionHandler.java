package com.rbs.primenumbers.api;

import com.rbs.primenumbers.domain.PrimesService;
import com.rbs.primenumbers.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("ValidationError", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("InvalidArgument", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(PrimesService.UpperBoundExceededException.class)
    public ResponseEntity<ErrorResponse> handleUpperBound(PrimesService.UpperBoundExceededException ex) {
        return ResponseEntity.unprocessableEntity().body(new ErrorResponse("UpperBoundExceeded", ex.getMessage(), Instant.now()));
    }
}

