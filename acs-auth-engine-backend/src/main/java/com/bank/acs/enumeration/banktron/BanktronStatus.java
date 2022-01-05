package com.bank.acs.enumeration.banktron;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum BanktronStatus {
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    ARCHIVED("Archived");

    @JsonValue
    private final String name;

    @SuppressWarnings("unused")
    @JsonCreator
    public static BanktronStatus fromStatusName(String statusName) {
        return Arrays.stream(values()).filter(v -> v.getName().equals(statusName)).findFirst().orElse(null);
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
