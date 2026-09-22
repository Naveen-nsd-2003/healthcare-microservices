package com.naveen.healthcare.authservice.util;

import java.security.SecureRandom;

public final class OtpGenerator {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private OtpGenerator() {
    }

    public static String generateOtp() {

        int otp = 100000 + RANDOM.nextInt(900000);

        return String.valueOf(otp);
    }
}