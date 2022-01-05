package com.bank.acs.util;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.substringBetween;
import static org.apache.commons.lang3.StringUtils.substringsBetween;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class CardUtil {

    private static final String MASK_SYMBOL = "*";

    public static String maskSensitiveInformation(String information) {
        return maskPersonalCodeInString(maskCardNumber(maskCardExpiryInString(information)));
    }

    public static String maskCardNumber(String cardNumber) {
        return maskCardNumber(cardNumber, false);
    }

    public static String maskCardNumber(String cardNumber, boolean maskFirstFour) {
        if (!StringUtils.isEmpty(cardNumber)){
            return cardNumber.replaceAll("([0-9]{4})[0-9]{8}([0-9]{4})", maskFirstFour ? "************$2" : "$1********$2");
        }
        return cardNumber;
    }

    public static String maskCardExpiryInString(String data) {
        if (StringUtils.isEmpty(data) || !(data.contains("expiry=") && data.contains("%"))) {
            return data;
        }
        String notMaskedExpiry = substringBetween(data, "expiry=", "%");
        String maskedExpiry = StringUtils.repeat(MASK_SYMBOL, notMaskedExpiry.length());
        return data.replace("expiry=" + notMaskedExpiry + "%", "expiry=" + maskedExpiry + "%");
    }

    public static String maskPersonalCodeInString(String data) {
        if (StringUtils.isEmpty(data)) {
            return data;
        }

        Set<String> notMaskedPersonalCodes = Pattern.compile("\\b(?:PersonCode|personCode|cardHolderPersonalCode|regCode)\\s*=\\s*['\"]*([^,]*)\\b")
                .matcher(data)
                .results()
                .map(matcher -> matcher.group(1))
                .collect(Collectors.toSet());

        if (data.contains("<person-code-card-holder>") && data.contains("</person-code-card-holder>")) {
            notMaskedPersonalCodes.addAll(Arrays.asList(substringsBetween(data, "<person-code-card-holder>", "</person-code-card-holder>")));
        }

        for(String personalCode: notMaskedPersonalCodes) {
            String maskedPersonalCode = maskPersonalCode(personalCode);
            data = data.replace(personalCode, maskedPersonalCode);
        }

        return data;
    }

    public static String maskPersonalCode(String notMaskedPersonalCode) {
        if (StringUtils.isEmpty(notMaskedPersonalCode) || notMaskedPersonalCode.contains(MASK_SYMBOL)) {
            return notMaskedPersonalCode;
        }

        String maskedPersonalCode;
        if (notMaskedPersonalCode.contains("-") && notMaskedPersonalCode.length() == 12) {
            maskedPersonalCode = notMaskedPersonalCode.replaceAll("(.{2}).{4}-(.{3}).{2}", "$1****-$2**");
        } else if (notMaskedPersonalCode.length() >= 11) {
            String replacement = "$1****$2" + StringUtils.repeat(MASK_SYMBOL, notMaskedPersonalCode.trim().length() - 8);
            maskedPersonalCode = notMaskedPersonalCode.replaceAll("(.{2}).{4}(.{2}).*", replacement);
        } else {
            char[] codeChars = notMaskedPersonalCode.toCharArray();
            for (int i = 0; i < codeChars.length; i++){
                if (i % 2 != 0) {
                    codeChars[i] = '*';
                }
            }
            maskedPersonalCode = String.valueOf(codeChars);
        }

        return maskedPersonalCode + " [sha256Hex=" + sha256Hex(notMaskedPersonalCode) + "]";
    }

    @SneakyThrows
    private static String sha256Hex(String stringToHash) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashedBytes);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
