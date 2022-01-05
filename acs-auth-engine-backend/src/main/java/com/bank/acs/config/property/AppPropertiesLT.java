package com.bank.acs.config.property;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.bank.acs.Profile.COUNTRY_LT_PROFILE;

@Getter
@Configuration
@Profile(COUNTRY_LT_PROFILE)
public class AppPropertiesLT {

    @Value("${app.banktron.sonic.hostname}")
    private String sonicHostname;

    @Value("${app.banktron.sonic.port}")
    private Integer sonicPort;

    public String getSonicUrl() {
        return sonicHostname + ":" + sonicPort;
    }
}
