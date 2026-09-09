package com.patricia.subscriptionApp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.*;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private Map<String, Object> buildBody(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> body = buildBody(HttpStatus.NOT_FOUND, ex.getMessage(), path);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        List<Map<String, String>> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> err = new HashMap<>();
            err.put("field", fieldError.getField());
            err.put("message", fieldError.getDefaultMessage());
            errors.add(err);
        }
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed", path);
        body.put("errors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Malformed JSON request", path);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, ex.getMessage(), path);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);
        Map<String, Object> body = buildBody(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", path);
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<Object> handleEmailAlreadyInUse(
            EmailAlreadyInUseException ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");

        Map<String, Object> body = buildBody(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(
            BadRequestException ex,
            WebRequest request
    ) {
        String path = request.getDescription(false).replace("uri=", "");

        Map<String, Object> body = buildBody(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                path
        );

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

}
