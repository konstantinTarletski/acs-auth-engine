package com.bank.acs.enumeration.banktron;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum BanktronSessionState {
    AUTHENTICATED("Authenticated"),
    TERMINATED("Terminated");

    @JsonValue
    private final String name;

    @SuppressWarnings("unused")
    @JsonCreator
    public static BanktronSessionState fromName(String stateName) {
        return Arrays.stream(values()).filter(v -> v.getName().equals(stateName)).findFirst().orElse(null);
    }
}
