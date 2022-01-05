package com.bank.acs.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.max-attempts")
public class MaxAttemptsProperties {

    private Integer forEnterLogin;
    private Integer forCheckSmartIdStatus;
    private Integer forChangeAuthMethod;

}
