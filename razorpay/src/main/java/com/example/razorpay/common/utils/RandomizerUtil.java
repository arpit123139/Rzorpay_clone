package com.example.razorpay.common.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String randomBase64(int length) {

        byte[] buf = new byte[length];
        SECURE_RANDOM.nextBytes(buf);   // basically this method will generate random number in the range  {-128, 127} and assign it to the byte Array
//        [4, 12, 100, -12]
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
