package com.bank.acs.enumeration.ciam;

import com.bank.acs.enumeration.AuthMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j

public enum CiamAuthMethod {

    SMARTID,
    MOBILEID,
    PINCALC,
    IDCARD;

    public static AuthMethod toAuthMethod(CiamAuthMethod ciamAuthMethod) {
        switch (ciamAuthMethod) {
            case SMARTID: {
                return com.bank.acs.enumeration.AuthMethod.SMART_ID;
            }
            case MOBILEID: {
                return com.bank.acs.enumeration.AuthMethod.M_SIGNATURE;
            }
            case PINCALC: {
                return com.bank.acs.enumeration.AuthMethod.CODE_CALCULATOR;
            }
            default: {
                log.warn("Can not convert CiamAuthMethod = {}, to AuthMethod", ciamAuthMethod);
                return null;
            }
        }
    }

    public static CiamAuthMethod toCiamAuthMethod(AuthMethod authMethod) {
        switch (authMethod) {
            case SMART_ID: {
                return CiamAuthMethod.SMARTID;
            }
            case M_SIGNATURE: {
                return CiamAuthMethod.MOBILEID;
            }
            case CODE_CALCULATOR: {
                return CiamAuthMethod.PINCALC;
            }
            default: {
                log.warn("Can not convert AuthMethod = {}, to BanktronAuthMethod", authMethod);
                return null;
            }
        }
    }

}
