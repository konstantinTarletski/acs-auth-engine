package com.bank.acs.enumeration.roofid;

import com.bank.acs.enumeration.AuthMethod;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum RoofIdAuthMethod {

    S,
    N,
    V;

    public static AuthMethod toAuthMethod(RoofIdAuthMethod roofIdAuthMethod) {
        if (roofIdAuthMethod == null) {
            return null;
        }

        switch (roofIdAuthMethod) {
            case S: {
                return AuthMethod.SMART_ID;
            }
            case N:
            case V: {
                return AuthMethod.CODE_CALCULATOR;
            }
            default: {
                log.warn("Can not convert RoofIdAuthMethod = {}, to AuthMethod", roofIdAuthMethod);
                return null;
            }
        }
    }

    public static RoofIdAuthMethod toRoofIdAuthMethod(AuthMethod authMethod) {
        switch (authMethod) {
            case SMART_ID: {
                return S;
            }
            case CODE_CALCULATOR: {
                return V; //just ANY type of code calculator
            }
            default: {
                log.warn("Can not convert AuthMethod = {}, to RoofIdAuthMethod", authMethod);
                return null;
            }
        }
    }

    public static RoofIdAuthMethod fromString(String name) {
        return Arrays.stream(values()).filter(v -> v.name().equals(name)).findFirst().orElse(null);
    }


}
