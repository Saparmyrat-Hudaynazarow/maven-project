package com.resource.exception;

import com.resource.exception.response.SimpleErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<SimpleErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.NOT_FOUND.value())
        );
        log.error("Resource not found:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidMp3Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleInvalidMp3Request(InvalidMp3Exception ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );

        log.error("InvalidMp3 data:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<SimpleErrorResponse> handleValidation(ValidationException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );

        log.error("Resource validation exception:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleGeneralException(Exception ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                "An error occurred on the server: ",
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        log.error("Unknown error",ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}