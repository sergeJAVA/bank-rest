package com.example.bankcards.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NumberEncryptionUtil {

    public static String encryptCardNumber(String cardNum) {
        return "**** **** **** " + cardNum.substring(12);
    }

}
