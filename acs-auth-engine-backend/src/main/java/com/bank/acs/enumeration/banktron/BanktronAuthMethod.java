package com.bank.acs.enumeration.banktron;

import com.bank.acs.enumeration.AuthMethod;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum BanktronAuthMethod {

    CODE_CALCULATOR(7),  // DP_250_VASCO
    GO3_VASCO(8),
    M_SIGNATURE(30),
    E_SIGNATURE(25),
    PIN_TAN_SMS(27),
    TAN(28),
    SMART_ID(29);

    @JsonValue
    private final int code;

    @SuppressWarnings("unused")
    @JsonCreator
    public static BanktronAuthMethod fromCode(Integer targetCode) {
        return Arrays.stream(values()).filter(v -> v.getCode() == targetCode).findFirst().orElse(null);
    }

    public static BanktronAuthMethod toBanktronAuthMethod(AuthMethod authMethod) {
        switch (authMethod) {
            case SMART_ID: {
                return BanktronAuthMethod.SMART_ID;
            }
            case M_SIGNATURE: {
                return BanktronAuthMethod.M_SIGNATURE;
            }
            case CODE_CALCULATOR: {
                return BanktronAuthMethod.CODE_CALCULATOR;
            }
            default: {
                log.warn("Can not convert AuthMethod = {}, to BanktronAuthMethod", authMethod);
                return null;
            }
        }
    }

    public static AuthMethod toAuthMethod(BanktronAuthMethod banktronAuthMethod) {
        switch (banktronAuthMethod) {
            case SMART_ID: {
                return AuthMethod.SMART_ID;
            }
            case M_SIGNATURE: {
                return AuthMethod.M_SIGNATURE;
            }
            case CODE_CALCULATOR: {
                return AuthMethod.CODE_CALCULATOR;
            }
            default: {
                log.warn("Can not convert BanktronAuthMethod = {}, to AuthMethod", banktronAuthMethod);
                return null;
            }
        }
    }
}
