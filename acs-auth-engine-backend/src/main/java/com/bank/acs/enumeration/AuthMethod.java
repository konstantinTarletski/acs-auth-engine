package com.bank.acs.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum AuthMethod {

    CODE_CALCULATOR("screens.choose.authMethods.codeCalculator","Generator","code-calculator",null),
    SMART_ID("screens.choose.authMethods.smartId","Smart-ID","smart-id",null),
    M_SIGNATURE("screens.choose.authMethods.mobileId","M-signature","mobile-id",null),
    ID_CARD("screens.choose.authMethods.idCard", "ID-Card", "id-card", null);

    private final String translationJsonPath;
    private final String internationalName;
    private final String icon;
    @Setter
    private String translationValue;

}
