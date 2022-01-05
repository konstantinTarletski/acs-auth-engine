package com.bank.acs.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AcsUiType {
    TEXT("01"),
    SINGLE_SELECT("02"),
    MULTI_SELECT("03"),
    OOB("04");

    @JsonValue
    private final String code;
}
