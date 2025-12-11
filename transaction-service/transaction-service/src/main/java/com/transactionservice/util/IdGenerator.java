package com.transactionservice.util;

import java.util.UUID;

public class IdGenerator {

    public static String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
