package com.accountservice.exception;

import com.accountservice.util.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------- Handle Account Not Found ----------------
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(AccountNotFoundException ex) {

        logger.warn("AccountNotFoundException: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), null, false),
                HttpStatus.NOT_FOUND
        );
    }

    // ---------------- Handle All Other Exceptions ----------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {

        logger.error("Unhandled Exception:", ex);

        return new ResponseEntity<>(
                new ApiResponse("Error: " + ex.getMessage(), null, false),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
