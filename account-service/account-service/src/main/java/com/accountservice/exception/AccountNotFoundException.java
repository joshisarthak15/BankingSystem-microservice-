package com.accountservice.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String acc) {
        super("Account not found: " + acc);
    }
}
