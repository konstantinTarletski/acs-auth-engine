package com.bank.acs.util;

import lombok.NoArgsConstructor;

import javax.validation.ConstraintValidatorContext;

import static java.lang.Character.isDigit;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class StringUtil {

    public static final int TRUNCATE_LENGTH = 300;

    public static String removeWhiteSpaces(String text) {
        return text == null ? null : text.replaceAll("\\s", "");
    }

    public static String truncate(String text, String suffix){
        String ret = null;
        if(text != null){
            ret = text.substring(0, Math.min(text.length(), TRUNCATE_LENGTH));
            if(text.length() > TRUNCATE_LENGTH && suffix != null){
                ret += suffix;
            }
        }
        return ret;
    }

    public static String removeLineBreaks(String text){
        return text != null ? text.replace("\n", "").replace("\r", "") : null;
    }

    public static boolean isPersonalCodeValid(String ownerPersonalCode) {
        boolean containsOnlyZeros = true;
        boolean containsMultipleDigits = false;

        for (char symbol: ownerPersonalCode.toCharArray()) {
            if (containsOnlyZeros && isDigit(symbol) && Integer.parseInt("" + symbol) != 0) {
                containsOnlyZeros = false;
            }
            if (isDigit(symbol)) {
                containsMultipleDigits = true;
            }
        }
        return !containsOnlyZeros && containsMultipleDigits;
    }

}
