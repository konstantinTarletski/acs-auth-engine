package com.bank.acs.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DeviceChannel {
    APP("01"),
    BROWSER("02"),
    THREE_DS("03");

    @JsonValue
    private final String code;

    @SuppressWarnings("unused")
    @JsonCreator
    public static DeviceChannel fromCode(String deviceChannelCode) {
        return Arrays.stream(values()).filter(v -> v.getCode().equals(deviceChannelCode)).findFirst().orElse(null);
    }
}
