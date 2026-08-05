package org.jejuro.miraero.domain.moneybox.util;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class MoneyBoxAccountNumberGenerator {
    private static final String PREFIX = "999";
    private static final int RANDOM_LENGTH = 10;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {

        StringBuilder accountNumber = new StringBuilder(PREFIX);

        for (int i = 0; i < RANDOM_LENGTH; i++) {
            accountNumber.append(
                    secureRandom.nextInt(10)
            );
        }

        return accountNumber.toString();
    }
}
