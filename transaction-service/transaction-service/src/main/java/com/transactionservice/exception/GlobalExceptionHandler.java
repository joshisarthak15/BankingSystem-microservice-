package com.transactionservice.exception;

import com.transactionservice.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------- Handle Known Business Exceptions ----------------
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ApiResponse> handleTxn(TransactionException ex) {

        logger.warn("TransactionException occurred: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ApiResponse(ex.getMessage(), null, false),
                HttpStatus.BAD_REQUEST
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
