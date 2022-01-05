package com.bank.acs.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * KBA = Knowledge-Based Authentication
 * OOB = Out-of-Band Authentication
 * OTP = One-Time Passcode
 * RBA = Risk-Based Authentication
 */
@Getter
@RequiredArgsConstructor
public enum AcsAuthenticationMethod {
    STATIC_PASSCODE("01"),
    SMS_OTP("02"),
    KEY_FOB_OR_EMV_CARD_READER_OTP("03"),
    APP_OTP("04"),
    OTP_OTHER("05"),
    KBA("06"),
    OOB_BIOMETRICS("07"),
    OOB_LOGIN("08"),
    OOB_OTHER("09"),
    OTHER("10"),
    FRICTIONLESS_FLOW_AND_RBA_REVIEW("97"),
    FRICTIONLESS_FLOW_AND_RBA("99");

    @JsonValue
    private final String code;

    @SuppressWarnings("unused")
    @JsonCreator
    public static AcsAuthenticationMethod fromCode(String authMethodCode) {
        return Arrays.stream(values()).filter(v -> v.getCode().equals(authMethodCode)).findFirst().orElse(null);
    }
}
