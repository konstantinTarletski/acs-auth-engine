package com.bank.acs.enumeration.banktron;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BanktronEndpoint {

    PERSONLOGINS("/services/lt/acs/personlogins"),
    AUTHENTICATE("/services/lt/acs/authenticate"),
    LOGOUT("/services/lt/acs/logout"),
    CHECKSTATUS("/services/lt/acs/checkstatus"),
    CONFIRMCREDENTIALS("/services/lt/acs/confirmcredentials"),
    ;

    private final String link;

}
