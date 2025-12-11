package com.transactionservice.exception;

public class TransactionException extends RuntimeException {
    public TransactionException(String msg) {
        super(msg);
    }
}
