package com.bank.acs.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ChallengeFlowType {

    APP_NATIVE(DeviceChannel.APP, "01"),
    APP_HTML(DeviceChannel.APP, "02"),
    BROWSER(DeviceChannel.BROWSER, "02");

    private final DeviceChannel channel;
    private final String acsInterface;

    public static final Optional<ChallengeFlowType> determinateChallengeFlowType(DeviceChannel channel, String acsInterface) {
        return Arrays.stream(ChallengeFlowType.values())
                .filter(flowType -> flowType.channel == channel && acsInterface.equalsIgnoreCase(flowType.acsInterface))
                .findAny();

    }

}
