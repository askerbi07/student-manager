package com.myproject.controller;

import com.myproject.repository.RepositoryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.BAD_REQUEST, exception.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ErrorResponse> handleRepositoryException(RepositoryException exception) {
        ErrorResponse body = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private record ErrorResponse(String timestamp, int status, String error, String message) {
        static ErrorResponse of(HttpStatus httpStatus, String message) {
            String safeMessage = message == null ? httpStatus.getReasonPhrase() : message;
            return new ErrorResponse(
                    Instant.now().toString(),
                    httpStatus.value(),
                    httpStatus.getReasonPhrase(),
                    safeMessage
            );
        }
    }
}
