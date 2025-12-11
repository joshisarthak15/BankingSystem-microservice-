package com.accountservice.util;

import java.util.Random;

public class IdGenerator {

    private static final Random rand = new Random();

    public static String generateAccountNumber(String holderName) {
        String prefix = holderName.substring(0, Math.min(3, holderName.length())).toUpperCase();
        int num = 1000 + rand.nextInt(9000);
        return prefix + num;
    }
}
